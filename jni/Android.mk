LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := dictionary
LOCAL_SRC_FILES := native.cpp dictionary/BasicTrie.cpp dictionary/TernaryTrie.cpp ann/Network.cpp ann/Layer.cpp ann/Neuron.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
