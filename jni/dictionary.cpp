#include <jni.h>
#include <android/log.h>
#include <vector>
#include <string>
#include <fstream>
#include <stdlib.h>
#include "sqlite3/sqlite3.h"
#include "dictionary/BasicTrie.h"

#define LOGI(info) __android_log_write(ANDROID_LOG_INFO,"JNI",info)
#define LOGE(error) __android_log_write(ANDROID_LOG_ERROR,"JNI",error)

extern "C" {
int JNICALL Java_pl_scribedroid_input_dictionary_NativeDictionary_createDictionary(
		JNIEnv* env, jobject thiz, jstring filename);
void JNICALL Java_pl_scribedroid_input_dictionary_NativeDictionary_closeDictionary(
		JNIEnv* env, jobject thiz, jint dict);
jobjectArray JNICALL Java_pl_scribedroid_input_dictionary_NativeDictionary_suggest(
		JNIEnv* env, jobject thiz, jint dict, jstring prefix, jint limit);
jboolean JNICALL Java_pl_scribedroid_input_dictionary_NativeDictionary_isValid(
		JNIEnv* env, jobject thiz, jint dict, jstring word);
}
;

static int callback(void *NotUsed, int argc, char **argv, char **azColName) {
	int i;
	for (i = 0; i < argc; i++) {
		LOGI(argv[i] ? argv[i] : "NULL");
	}
	return 0;
}

int Java_pl_scribedroid_input_dictionary_NativeDictionary_createDictionary(
		JNIEnv* env, jobject thiz, jstring filename) {
	const char* filename_chars = env->GetStringUTFChars(filename, 0);

	LOGI("Start creating dictionary");
	BasicTrie * dict = new BasicTrie();
	/*std::ifstream is(filenameChars);

	 std::string line;
	 while (getline(is,line)) {
	 dict->add(line);
	 }*/

	sqlite3 *db;
	int rc;
	char* error_message;
	const char* sql_text = "select word from words";
	int sql_length = strlen(sql_text) + 1;
	sqlite3_stmt* sql_statement;

	rc = sqlite3_open(filename_chars, &db);
	if (rc) {
		LOGE("Can't open database");
		sqlite3_close(db);
		return 0;
	}
	LOGI(filename_chars);
	//rc = sqlite3_exec(db, "select word from words;", callback, 0, &error_message);
	sqlite3_prepare_v2(db, sql_text, sql_length, &sql_statement, NULL);

	while (true) {
		rc = sqlite3_step(sql_statement);
		if (rc == SQLITE_ROW) {
			int bytes;
			std::string word = std::string(
					reinterpret_cast<const char*>(sqlite3_column_text(
							sql_statement, 0)));
			dict->add(word);
		}
		else if (rc == SQLITE_DONE) {
			break;
		}
		else {
			LOGE("SQL Failed");
			break;
		}
	}

	LOGI("Executed");

	sqlite3_finalize(sql_statement);
	sqlite3_close(db);
	LOGI("Closed");
	env->ReleaseStringUTFChars(filename, filename_chars);

	LOGI("End creating dictionary");

	return (jint) dict;
}

void Java_pl_scribedroid_input_dictionary_NativeDictionary_closeDictionary(
		JNIEnv* env, jobject thiz, jint dict) {
	delete (BasicTrie*) dict;
}

jobjectArray Java_pl_scribedroid_input_dictionary_NativeDictionary_suggest(
		JNIEnv* env, jobject thiz, jint dict, jstring prefix, jint limit) {
	LOGI("Native suggest");
	jobjectArray result = NULL;
	jobjectArray strArray = NULL;
	jclass stringClass = NULL;
	BasicTrie* dictionary = (BasicTrie*) dict;

	const char* prefixChars = env->GetStringUTFChars(prefix, 0);

	std::vector<std::string> suggestions = dictionary->suggest(prefixChars,
			limit);

	if (suggestions.size() > 0) {
		stringClass = env->FindClass("java/lang/String");

		strArray = env->NewObjectArray(suggestions.size(), stringClass, NULL);

		if (env->ExceptionCheck()) {
			fprintf(stderr,
					"Got exception while creating String array or getting String class\n");
			env->DeleteLocalRef(stringClass);
			env->DeleteLocalRef(strArray);
		}

		int i = 0;
		for (std::vector<std::string>::iterator it = suggestions.begin();
				it != suggestions.end(); ++it) {
			jstring argStr;
			argStr = env->NewStringUTF((*it).c_str());
			LOGI((*it).c_str());
			if (env->ExceptionCheck()) {
				fprintf(stderr, "Got exception while allocating string\n");
				env->DeleteLocalRef(stringClass);
				env->DeleteLocalRef(strArray);
			}
			env->SetObjectArrayElement(strArray, i, argStr);
			env->DeleteLocalRef(argStr);
			i++;
		}

		result = strArray;
		strArray = NULL;

		env->DeleteLocalRef(stringClass);
		env->DeleteLocalRef(strArray);
	}

	env->ReleaseStringUTFChars(prefix, prefixChars);

	return result;
}

jboolean Java_pl_scribedroid_input_dictionary_NativeDictionary_isValid(
		JNIEnv* env, jobject thiz, jint dict, jstring word) {
	BasicTrie* dictionary = (BasicTrie*) dict;

	const char *wordChars = env->GetStringUTFChars(word, 0);

	jboolean result = dictionary->isValid(wordChars);

	env->ReleaseStringUTFChars(word, wordChars);
	return result;
}
