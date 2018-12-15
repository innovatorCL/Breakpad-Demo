# 设置Cmake最小版本
cmake_minimum_required(VERSION 3.4.1)

# 编译library
add_library(

        # 设置library名称
        crash-lib

        # 设置library模式
        # SHARED模式会编译so文件，STATIC模式不会编译
        SHARED

        # 设置原生代码路径
        src/main/cpp/crash.cpp)

# 定位library
find_library(

        # library名称
        log-lib

        # Specifies the name of the NDK library that you want CMake to locate.
        log )

# 关联library
target_link_libraries(

        # 关联的library
        crash-lib

        # 关联crash-lib和log-lib
        ${log-lib} )

