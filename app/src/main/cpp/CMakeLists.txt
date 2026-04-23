cmake_minimum_required(VERSION 3.22.1)
project("whisper")

set(CMAKE_CXX_STANDARD 17)

add_library(
    whisper
    SHARED
    whisper_jni.cpp
)

find_library(log-lib log)

target_link_libraries(whisper ${log-lib})
