package com.cobox.vulture.ddnsserver

import java.net.InetAddress

interface DdnsAdapter {

    fun initVendor()

    fun releaseVendor()

    /**
     * 更新A记录
     * @param domain 要更新的域名
     * @param address 要更新的IP地址
     * @return DNS服务器返回的更新是否成功的回执
     */
    fun updateARecord(domain: String, address: InetAddress, result: ((isSuccess: Boolean) -> Unit))

    /**
     * 查询A记录
     * @param domain 要查询的域名
     * @return 域名对应的A记录的IP地址
     */
    fun queryARecord(domain: String, result: ((ip: InetAddress) -> Unit))

}