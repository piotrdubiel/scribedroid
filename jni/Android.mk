LOCAL_PATH:= $(call my-dir)

# build sqlite3 

include $(CLEAR_VARS)

LOCAL_MODULE    	:= sqlite3
LOCAL_C_INCLUDES 	:= $(LOCAL_PATH)/sqlite3
LOCAL_SRC_FILES		:= sqlite3/sqlite3.c

include $(BUILD_STATIC_LIBRARY)

# build libdictionary

include $(CLEAR_VARS)

LOCAL_MODULE    		:= dictionary
LOCAL_SRC_FILES 		:= dictionary.cpp dictionary/BasicTrie.cpp dictionary/TernaryTrie.cpp
LOCAL_LDLIBS    		:= -llog
LOCAL_STATIC_LIBRARIES	:= libsqlite3

include $(BUILD_SHARED_LIBRARY)

# build libann

include $(CLEAR_VARS)

LOCAL_MODULE    := ann
LOCAL_SRC_FILES := network.cpp ann/Network.cpp ann/Layer.cpp ann/Neuron.cpp

include $(BUILD_SHARED_LIBRARY)