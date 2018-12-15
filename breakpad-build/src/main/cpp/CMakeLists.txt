cmake_minimum_required(VERSION 3.4.1)
project(breakpad-core)

set(ENABLE_INPROCESS ON)
set(ENABLE_OUTOFPROCESS ON)
set(ENABLE_LIBCORKSCREW ON)
set(ENABLE_LIBUNWIND ON)
set(ENABLE_LIBUNWINDSTACK ON)
set(ENABLE_CXXABI ON)
set(ENABLE_STACKSCAN ON)

if (${ENABLE_INPROCESS})
    add_definitions(-DENABLE_INPROCESS)
endif ()
if (${ENABLE_OUTOFPROCESS})
    add_definitions(-DENABLE_OUTOFPROCESS)
endif ()


set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Werror=implicit-function-declaration")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 ")

# breakpad
include_directories(external/libbreakpad/src external/libbreakpad/src/common/android/include)
add_subdirectory(external/libbreakpad)
list(APPEND LINK_LIBRARIES breakpad)

# 编译library
add_library(

        # 设置library名称
        breakpad-core

        # 设置library模式
        # SHARED模式会编译so文件，STATIC模式不会编译
        SHARED

        # 设置原生代码路径
        breakpad.cpp)

# 关联library
target_link_libraries(

        breakpad-core

        # 关联breakpad-core和LINK_LIBRARIES
        ${LINK_LIBRARIES}

        log)