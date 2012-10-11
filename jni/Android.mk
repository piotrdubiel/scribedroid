LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := dictionary
LOCAL_SRC_FILES := native.cpp BasicTrie.cpp TernaryTrie.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
