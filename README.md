[![](https://github.com/Mysterious-organization/Vulture-Station/workflows/build/badge.svg)](https://github.com/Mysterious-organization/Vulture-Station/actions)

# Vulture编程指南

### 目录
 - [开始编程](/doc/GetStart.md)
 - [工程结构](/doc/ProjectStructure.md)
 - [模块介绍](/doc/Modules.md)
 - [开发新的模块](/doc/CreateNewModule.md)
 - [部署](/doc/Deployment.md)

## 简介

Vulture是个C/S的私人服务解决方案，它的主要模块包括：
 - 私人NAS
 - 私人IOT设备云
 - 私人IM

整个Vulture有一个服务端、数个客户端及数个设备端组成。

其中服务端可以是如下配置：
 - 单一设备构建的服务器
 - 多个设备组成的服务器集群

客户端包含：
 - Android客户端
 - Windows Desktop客户端
 - Linux Desktop客户端
 - MacOS Desktop客户端
 - Web客户端

设备端作为可接入Vulture IOT设备云中的设备，支持：
 - ESP8266/ESP32设备端 （由ESP8266/ESP32 Vulture SDK提供支持）
 - Linux设备端 （由Linux Vulture SDK提供支持）

Vulture具备向DDNS自举IP地址的能力，一旦向自己的ISP申请来公网IP，并配置了DDNS服务，您则具备了将Vulture公布到公网的能力。
届时您可能需要对相应的法律法规负责。

## 私人NAS

此模块为个人提供了私有化的网络云盘，可以将自己的文件保存在服务器上随时访问。
同时它也支持您邀请自己的家人或好友在自己的私人服务器上创建他们自己的账号，以便
亲朋好友可以共享一台NAS服务器。

#### 1. 文件共享
在同一台Vulture的NAS服务器上创建的账号，它们的存储是相互独立的，各自相互不能访问其他账号存储的文件。
但如果需要与其他账号共享文件，可以使用NAS对文件、文件夹对共享功能来达成。

文件、文件夹对共享功能对DLNA设备有效，您可以向附近可见对DLNA设备共享NAS中对文件或文件夹。

#### 2. 离线下载
您可以向NAS上传已经存在对文件，也可以向NAS上传一个离线文件，并指定此离线文件对网络连接，Vulture的NAS服务会为您自动下载此链接对应的文件到NAS服务器中，供您以后使用。

#### 3. 在线浏览
Vulture的客户端可以在线浏览NAS中的文件。
针对存储在NAS中的媒体文件，可以直接在客户端中在线预览。
预览的过程中随时可以下载预览的文件到客户端所在的设备中。

## 设备云IOT

此模块支持由Vulture SDK支持的嵌入式设备或Linux设备接入Vulture服务器。
在Vulture服务器上创建的每一个账号都可拥有自己管辖的设备。且设备间可以通过Vulture服务器相互了解其他设备的状况，以作出对应的响应。

#### 1. 设备共享
账号间的设备默认相互不可见，但可以通过设备共享将自己的设备共享给Vulture服务器中的其他账号使用。

***当您共享自己的设备给其他账号后，其他账户可以使用您共享的设备所提供的信息来让自己的设备作出特定的响应，也可使用自己设备的信息来控制您共享的设备作出特定的响应。**

#### 2. 设备信息展示
Vulture IOT设备云支持灵活的数据协议，借此特性您可以对接入您Vulture IOT设备云的设备所提供的数据做可配置的可视化预览。

1. 您可以指定所有数据字段的名称，以便您方便辨认
2. 针对单一字段的数据，您可以为其指定单位并选择提供的展示方式
   - 如数据为温度值，您可以选择用柱状图或管状图来展示它
   - 如数据为速度值，您可以选择使用指针表来展示它
   - 如数据为重力值，您可以选择使用波形图来展示它 
3. 针对多维数据，如果可能，您可以选择图片来展示它
   - 多维数据应能够对当作矩阵或至少是稀疏矩阵对待
   - 您需要指定每个维度的值域或值域映射到图片通道上的方式
   - 您需要指定每个维度应对应到图片的哪个通道上展示
4. 多维数据同样可以使用如下图表来呈现
   - 多维度2D曲线图
   - 2D柱形图
   - 2D雷达图
   - 2D饼图/环图
   - 3D等高图
   - 3D波形图
5. 针对图片、视频可直接预览

#### 3. NAS共享
你可以在NAS中为特定的设备指定一个空间来存放设备产生的数据。
这些数据可以是视频文件、图片文件、文本文件，并支持把它们移动到NAS中永久存储。

设备在NAS上存放文件的空间支持下列几种方式来达成限制空间大小的目的：
 - 堆栈式存储策略
   - 当存储的空间被占满时，最后存储到空间中的文件将会被拒绝或丢弃
   - 如果空间中仅一个文件，则此文件写入的字节占满空间时，后续的写入会被拒绝
 - 队列式存储策略
   - 当存储的空间被占满时，最先存储到空间中的文件将会被丢弃
   - 如果空间中仅一个文件，则此文件写入的字节占满空间时，后续的写入会被拒绝

### 目标部署设备
