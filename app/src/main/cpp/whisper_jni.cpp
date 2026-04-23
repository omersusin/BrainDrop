#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_brain_drop_ml_WhisperJNI_transcribeFromFile(JNIEnv* env, jobject thiz, jstring modelPath, jstring audioPath) {
    return env->NewStringUTF("");
}

extern "C" JNIEXPORT jstring JNICALL
Java_brain_drop_ml_WhisperJNI_transcribeFromBuffer(JNIEnv* env, jobject thiz, jstring modelPath, jbyteArray audioData) {
    return env->NewStringUTF("");
}
