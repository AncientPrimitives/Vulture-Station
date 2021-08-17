class Mem {
    totalMem: number = 0
    availableMem: number = 0
    freeMem: number = 0
    totalSwap: number = 0
    freeSwap: number = 0
}

class DrawingDriver {
    ctx: CanvasRenderingContext2D | null
    canvas: HTMLCanvasElement

    hasInvalidate: boolean = false
    hasResize: boolean = false
    clientWidth: number = 0
    clientHeight: number = 0

    constructor(canvas: HTMLCanvasElement) {
        this.canvas = canvas
        this.ctx = canvas.getContext("2d")
        canvas.addEventListener("onresize", this.resize)
        canvas.addEventListener("oninvalid", this.invalidate)
    }

    drawInner() {
        this.hasInvalidate = false
        if (this.ctx) {
            this.onDraw(this.ctx)
        }
    }

    resizeInner() {
        this.hasResize = false
        if (this.canvas) {
            if ((this.canvas.clientWidth != this.clientWidth) || (this.canvas.clientHeight != this.clientHeight)) {
                this.clientWidth = this.canvas.clientWidth
                this.clientHeight = this.canvas.clientHeight
                this.canvas.width = this.clientWidth
                this.canvas.height = this.clientHeight
                console.log("[resizeInner] " + this.canvas.width + "x" + this.canvas.height)
                this.onResize(this.clientWidth, this.clientHeight)
            }
        }
    }

    requestResize() {
        if (this.hasResize) return
        const instance = this
        requestAnimationFrame(() => {
            instance.resizeInner()
            instance.drawInner()
        })
    }

    requestRender() {
        if (this.hasInvalidate) return
        const instance = this
        requestAnimationFrame(() => {
            instance.drawInner()
        })
    }

    resize(event: UIEvent | Event) {
        this.requestResize()
    }

    invalidate(event: UIEvent | Event) {
        this.requestRender()
    }

    onResize(width: number, height: number) { }

    onDraw(ctx: CanvasRenderingContext2D) { }
}

class MemSystemMonitor extends DrawingDriver {
    mem: Mem = new Mem()

    memMeter: CircleMeter = new CircleMeter(0.0, 1.0, false, 1)
    swapMeter: CircleMeter = new CircleMeter(0.0, 1.0, false, 1)

    constructor(canvas: HTMLCanvasElement) {
        super(canvas)
        this.createMemMeter()
    }

    createMemMeter() {
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
        this.requestResize()
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
        ctx.clearRect(0, 0, this.canvas.width, this.canvas.height)
        this.memMeter.draw(ctx)
        this.swapMeter.draw(ctx)
    }

}