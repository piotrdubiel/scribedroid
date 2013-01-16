#include <jni.h>
#include <fstream>
#include <stdlib.h>
#include "ann/Network.h"

//#define LOGI(info) __android_log_write(ANDROID_LOG_INFO,"JNI",info)
//#define LOGE(error) __android_log_write(ANDROID_LOG_ERROR,"JNI",error)


extern "C" {
int Java_pl_scribedroid_input_ann_NativeNetwork_load(JNIEnv* env, jobject thiz,jstring filename);
void Java_pl_scribedroid_input_ann_NativeNetwork_close(JNIEnv* env, jobject thiz,jint net);
jfloatArray Java_pl_scribedroid_input_ann_NativeNetwork_answer(JNIEnv* env, jobject thiz, jfloatArray in);
};
/*
int Java_pl_scribedroid_input_dictionary_NativeDictionary_createDictionary(JNIEnv* env, jobject thiz,jstring filename) {
	const char* filenameChars = env->GetStringUTFChars(filename, 0);

	LOGI("Start creating dictionary");
	BasicTrie * dict=new BasicTrie();
	std::ifstream is(filenameChars);

	std::string line;
	while (getline(is,line)) {
		dict->add(line);
	}

	env->ReleaseStringUTFChars(filename,filenameChars);

	return (jint) dict;
}

void Java_pl_scribedroid_input_dictionary_NativeDictionary_closeDictionary(JNIEnv* env, jobject thiz,jint dict) {
	delete (BasicTrie*) dict;
}

jobjectArray Java_pl_scribedroid_input_dictionary_NativeDictionary_suggest(JNIEnv* env, jobject thiz,jint dict,jstring prefix,jint limit) {
	LOGI("Native suggest");
	jobjectArray result = NULL;
    jobjectArray strArray = NULL;
	jclass stringClass = NULL;
	BasicTrie* dictionary=(BasicTrie*) dict;

	const char* prefixChars = env->GetStringUTFChars(prefix, 0);

	std::vector<std::string> suggestions=dictionary->suggest(prefixChars,limit);

	if (suggestions.size()>0) {
		stringClass = env->FindClass("java/lang/String");

		strArray = env->NewObjectArray(suggestions.size(), stringClass, NULL);

		if (env->ExceptionCheck()) {
			fprintf(stderr, "Got exception while creating String array or getting String class\n");
			env->DeleteLocalRef(stringClass);
			env->DeleteLocalRef(strArray);
		}

		int i=0;
		for (std::vector<std::string>::iterator it=suggestions.begin();it!=suggestions.end();++it) {
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

	env->ReleaseStringUTFChars(prefix,prefixChars);


    return result;
}

jboolean Java_pl_scribedroid_input_dictionary_NativeDictionary_isValid(JNIEnv* env, jobject thiz,jint dict,jstring word) {
	BasicTrie* dictionary=(BasicTrie*) dict;

	const char *wordChars = env->GetStringUTFChars(word, 0);

	jboolean result = dictionary->isValid(wordChars);

	env->ReleaseStringUTFChars(word,wordChars);
	return result;
}*/
