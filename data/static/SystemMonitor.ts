import {
    DrawingDriver
} from "./HUD/HUD"

import {
    CircleMeter
} from "./HUD/CircleMeter"

class Mem {
    totalMem: number = 0
    availableMem: number = 0
    freeMem: number = 0
    totalSwap: number = 0
    freeSwap: number = 0
}

export class MonitorPanel extends DrawingDriver {
    title: string = ""

    style = {
        title: {
            fontSize: 14,
            font: "bold {1}px HUD",
            fontColor: "#FFFFFF",
            titleMarginTop: 8,
            titleMarginStart: 0,
            hintLineLength: 50,
            hintLineColor:"#FFFFFF",
            hintLineAlpha: 1
        },
        frame: {
            frameLineWidth: 2,
            frameLineColor: "#FFFFFF",
            frameLineAlpha: 0.1,
            frameLineCap: "square"
        }
    }

    private topFramePath: Path2D | null = null
    private titleHintPath: Path2D | null = null

    constructor(title: string, canvas: HTMLCanvasElement) {
        super(canvas)
        this.title = title
    }

    onResize(width: number, height: number) {
        super.onResize(width, height)

        let y = this.style.frame.frameLineWidth * 0.5;
        this.topFramePath = new Path2D();
        this.topFramePath.moveTo(0, y);
        this.topFramePath.lineTo(width, y);

        this.titleHintPath = new Path2D();
        this.titleHintPath.moveTo(0, y);
        this.titleHintPath.lineTo(this.style.title.hintLineLength, y);
    }

    onDraw(ctx: CanvasRenderingContext2D) {
        ctx.clearRect(0, 0, this.canvas.width, this.canvas.height)
        super.onDraw(ctx);
        this.drawFrame(ctx);
        this.drawTitle(ctx);
    }

    protected drawTitle(ctx: CanvasRenderingContext2D) {
        if (this.title) {
            ctx.font = this.style.title.font.replace("{1}", this.style.title.fontSize.toString())
            ctx.fillStyle = this.style.title.fontColor
            ctx.textAlign = "start"

            const metrics = ctx.measureText(this.title)
            const baseline = metrics.actualBoundingBoxAscent
            ctx.fillText(
                this.title,
                this.style.title.titleMarginStart,
                baseline + this.style.title.titleMarginTop
            )
        }
    }

    protected drawFrame(ctx: CanvasRenderingContext2D) {
        // top
        // @ts-ignore
        ctx.lineCap = this.style.frame.frameLineCap
        ctx.lineWidth = this.style.frame.frameLineWidth
        if (this.topFramePath) {
            ctx.strokeStyle = this.style.frame.frameLineColor
            ctx.globalAlpha = this.style.frame.frameLineAlpha
            ctx.stroke(this.topFramePath)
        }
        if (this.titleHintPath) {
            ctx.strokeStyle = this.style.title.hintLineColor
            ctx.globalAlpha = this.style.title.hintLineAlpha
            ctx.stroke(this.titleHintPath)
        }
    }
}

/**
 * 内存面板
 */
export class MemoryMonitor extends MonitorPanel {
    mem: Mem = new Mem()

    memMeter: CircleMeter = new CircleMeter(0.0, 1.0, false, 1)
    swapMeter: CircleMeter = new CircleMeter(0.0, 1.0, false, 1)

    constructor(title: string, canvas: HTMLCanvasElement) {
        super(title, canvas)
        this.createMemMeter()
    }

    private createMemMeter() {
        this.memMeter.attachToRoot(this)
        this.swapMeter.attachToRoot(this)
        this.memMeter.hintAutoFontWeight = 0.25
        this.swapMeter.hintAutoFontWeight = 0.25
        this.memMeter.selectHintProgress(0, this.convertProgress)
        this.swapMeter.selectHintProgress(0, this.convertProgress)
    }

    convertProgress(progress: number): string {
        return (progress * 100).toFixed(1).toString() + "%"
    }

    updateMemInfo(
        totalMem: number, availableMem: number, freeMem: number,
        totalSwap: number, freeSwap: number
    ) {
        this.mem.totalMem = totalMem
        this.mem.availableMem = availableMem
        this.mem.freeMem = freeMem
        this.mem.totalSwap = totalSwap
        this.mem.freeSwap = freeSwap

        this.memMeter.updateActualProgress(0, (totalMem - availableMem) / totalMem)
        this.swapMeter.updateActualProgress(0, (totalSwap - freeSwap) / totalSwap)
        this.requestRender()
    }

    onResize(width: number, height: number) {
        super.onResize(width, height);

        let size = Math.min(width, height) * 0.65
        let spacing = (height - size) * 0.5
        let swpX = width - size - spacing
        let swpY = (height - size) * 0.5
        let memX = swpX - size - spacing
        let memY = swpY

        this.memMeter.setPosition(memX, memY)
        this.memMeter.updateSize(size, size)

        this.swapMeter.setPosition(swpX, swpY)
        this.swapMeter.updateSize(size, size)
    }

    onDraw(ctx: CanvasRenderingContext2D) {
        super.onDraw(ctx);
        this.memMeter.draw(ctx)
        this.swapMeter.draw(ctx)
    }

}

/**
 * CPU面板
 */
export class CpuMonitor extends MonitorPanel {

    constructor(title: string, canvas: HTMLCanvasElement) {
        super(title, canvas)
        this.createCpuMeter()
    }

    private createCpuMeter() {

    }

    updateCpuInfo() {
        this.requestRender()
    }
}

/**
 * 环境状态面板
 */
export class EnvMonitor extends MonitorPanel {

    constructor(title: string, canvas: HTMLCanvasElement) {
        super(title, canvas)
        this.createEnvMeter()
    }

    private createEnvMeter() {

    }

    updateEnvInfo() {
        this.requestRender()
    }
}

/**
 * 视频监控面板
 */
export class CCTVMonitor extends MonitorPanel {

    constructor(title: string, canvas: HTMLCanvasElement) {
        super(title, canvas)
    }

    updateCCTVInfo() {
        this.requestRender()
    }
}