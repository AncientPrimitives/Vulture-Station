var SystemMonitor = null;

let cpuMonitor = null;
let memMonitor = null;
let camMonitor = null;
let envMonitor = null;
let monitors = [camMonitor, cpuMonitor, memMonitor, envMonitor]

let monitorJob = null;

function onPageLoad() {
    if (!SystemMonitor) return;

    cpuMonitor = new SystemMonitor.CpuMonitor("CPU", document.getElementById("cpu_pane"))
    memMonitor = new SystemMonitor.MemoryMonitor("MEM", document.getElementById("mem_pane"))
    camMonitor = new SystemMonitor.CCTVMonitor("CCTV", document.getElementById("cam_pane"))
    envMonitor = new SystemMonitor.EnvMonitor("Env", document.getElementById("env_pane"))

    monitorJob = setInterval(function() {
        loadXMLDoc(function (data) {
            camMonitor.updateCCTVInfo()
            cpuMonitor.updateCpuInfo()
            memMonitor.updateMemInfo(
                data.meminfo.totalMem,
                data.meminfo.availableMem,
                data.meminfo.freeMem,
                data.meminfo.totalSwap,
                data.meminfo.freeSwap
            )
            envMonitor.updateEnvInfo()
        })
    }, 1000)
}

function onPageResize() {
    for (let i = 0; i < monitors.length; i++) {
        if (monitors[i]) monitors[i].requestResize()
    }
}

function onPageInvalidate() {
    for (let i = 0; i < monitors.length; i++) {
        if (monitors[i]) monitors[i].requestRender()
    }
}

function loadXMLDoc(dataCallback) {
    var xmlhttp;
    if (window.XMLHttpRequest) {
        // IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp = new XMLHttpRequest();
    } else {
        // IE6, IE5
        xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
    }

    xmlhttp.onreadystatechange = function() {
        if ((xmlhttp.readyState === XMLHttpRequest.DONE) && (xmlhttp.status === 200)) {
            dataCallback(eval("(" + xmlhttp.responseText + ")"));
        }
    }
    xmlhttp.open("GET", "/system/summary", true);
    xmlhttp.send();
}

require(["/SystemMonitorPage.js"], function(moduleSystemMonitorPage) {
    require(["SystemMonitor"], function (system_monitor) {
        SystemMonitor = system_monitor;
        window.addEventListener("load", onPageLoad)
        window.addEventListener("resize", onPageResize)
        window.addEventListener("invalid", onPageInvalidate)

        if (document.readyState === "complete") {
            onPageLoad()
        }
    })
})