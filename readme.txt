﻿java bean生成系统简介

本程序为开源授权，主要实现是从json格式生成javaBean的java文件的功能。

1、从http请求，get方法获取到的实时json格式网络数据；

2、从http请求，post方法获取到的实时json格式网络数据；

3、已保存文件获取到的json格式文件数据；



-----------------------------------------------------------------------------------
1、增加生成请求参数java文件的功能；


--------------------------------2016年3月16日

1、 修复json中list解析不正确的问题；

2、 增加requstHeader生成java文件

3、 增加了对Gson的支持；

4、修复异常状态的数组越界

--------------------------------2016年3月17日

1、增加了数组没有内容的 默认为HashMap<String,Object>类型的

2、修复了类型判断不准确的问题

3、增加了import HashMap的包

4、修复了Gson解析不正确的BUG；


--------------------------------2016年3月21日

1、修复一些界面布局不正确的问题
