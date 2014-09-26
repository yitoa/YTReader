#include <string.h>
#include <jni.h>
#include <dlfcn.h>
#include <android/log.h>
#include <stdlib.h>

#define  LOG_TAG    "grepJni"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

JNIEXPORT void JNICALL Java_com_yt_reader_optionmenu_TextSearch_grep
(JNIEnv *env, jobject obj, jstring jtext, jstring jpath, jstring jlogpath)
{

	LOGI("open so success!");
	char* text = (*env)->GetStringUTFChars(env, jtext, NULL);
	char* path = (*env)->GetStringUTFChars(env, jpath, NULL);
	char* logpath = (*env)->GetStringUTFChars(env, jlogpath, NULL);
	char* sa[] = {"grep", "-b", text, path, logpath};
	mysearch(5,sa);

}
