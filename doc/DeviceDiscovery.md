# 设备发现

VultureStation采用mDNS协议来实现局域网中的设备服务发现。
协议的具体细节可参考：[DNS-SD](https://www.dns-sd.org)

发现协议：
```
服务名称 = "Vulture.Station"
服务类型 = "_gateway._vulture._http._tcp"
发现端口 = "9201"
```
