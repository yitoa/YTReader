#获取当前目录
LOCAL_PATH := $(call my-dir)
#清除一些变量
include $(CLEAR_VARS)
#要生成的库名
LOCAL_MODULE    := grep
#库对应的源文件
LOCAL_SRC_FILES := alloca.c dfa.c grep.c obstack.c search.c getopt.c kwset.c regex.c grepJni.c
#libtest.so需要引用的库libdl.so:加载动态函数需要，liblog.so 日志打印需要，默认是system/lib目录下
LOCAL_LDLIBS := -ldl -llog 
#生成动态库libgrep.so
include $(BUILD_SHARED_LIBRARY)
