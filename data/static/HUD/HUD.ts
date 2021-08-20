export class Rect {
    left: number;
    top: number;
    right: number;
    bottom: number;

    constructor(left: number, top: number, right: number, bottom: number) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    x(): number { return this.left }
    y(): number { return this.top }
    width(): number { return (this.right - this.left) }
    height(): number { return (this.bottom - this.top) }
    centerX(): number { return (this.left + this.width() * 0.5) }
    centerY(): number { return (this.top + this.height() * 0.5) }
}

export class Math2D {
    static toDegree(radius: number): number {
        return 180.0 * radius / Math.PI
    }

    static toRadius(degrees: number): number {
        return degrees * Math.PI / 180
    }
}

export class DrawingDriver {
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
        if (this.needResize()) {
            this.requestResize();
        }

        this.hasInvalidate = false
        if (this.ctx) {
            this.onDraw(this.ctx)
        }
    }

    private needResize() {
        return (this.canvas.clientWidth != this.clientWidth) || (this.canvas.clientHeight != this.clientHeight);
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

export class DrawingObject {
    static AUTO_SIZE: number = -1

    clientArea: Rect = new Rect(0, 0, DrawingObject.AUTO_SIZE, DrawingObject.AUTO_SIZE);
    root: DrawingDriver | null = null

    attachToRoot(root: DrawingDriver) {
        this.root = root
    }

    setPosition(x: number, y: number) {
        const offsetX = x - this.clientArea.left;
        const offsetY = y - this.clientArea.top;
        this.clientArea.left += offsetX
        this.clientArea.right += offsetX
        this.clientArea.top += offsetY
        this.clientArea.bottom += offsetY
    }

    updateSize(width: number, height: number) {
        this.clientArea.right = this.clientArea.left + width
        this.clientArea.bottom = this.clientArea.top + height
    }

    draw(ctx: CanvasRenderingContext2D) {
        ctx.save()
        ctx.translate(this.clientArea.left, this.clientArea.top)
        this.onDraw(ctx)
        ctx.restore()
    }

    onDraw(ctx: CanvasRenderingContext2D) { }

    invalidate() {
        if (this.root) {
            this.root.requestRender()
        }
    }
}

// class Label extends DrawingObject {
//
//     style = {
//         contentStyle: "#FFFFFF",
//         contentAlpha: 1,
//         contentFont: "bold {1} HUD",
//         contentFontSize: 0.8,
//         contentFontUnit: "em",
//
//         hintSize: 1, // rect: 边长, circle: 直径
//         hintMarginEnd: 1
//     }
//
//     content: string
//     hintColor: string | CanvasGradient | CanvasPattern | null = null
//     contentFontSize: number = this.style.contentFontSize
//     contentFontUnit: string = this.style.contentFontUnit
//
//     hasMeasuredClientArea: boolean = false
//     hintPath: Path2D | null = null
//
//     constructor(content: string, hintColor?: string) {
//         super();
//         this.content = content
//         this.hintColor = hintColor || ""
//     }
//
//     setFontSize(fontSize: number, fontUnit: string = "em") {
//         this.contentFontSize = fontSize
//         this.contentFontUnit = fontUnit
//         this.hasMeasuredClientArea = false
//     }
//
//     setFontStyle(fontFillStyle: string) {
//         this.style.contentStyle = fontFillStyle
//         this.hasMeasuredClientArea = false
//     }
//
//     draw(ctx: CanvasRenderingContext2D) {
//         super.draw(ctx);
//         this.measureClientArea(ctx);
//
//         ctx.save()
//         ctx.font = this.style.contentFont.replace("{1}", (this.style.contentFontSize + this.style.contentFontUnit))
//         const metrics = ctx.measureText(this.content)
//
//         if (this.hintColor) {
//             ctx.translate()
//         }
//
//         ctx.restore()
//     }
//
//     protected measureClientArea(ctx: CanvasRenderingContext2D) {
//         if (this.hasMeasuredClientArea) return
//
//         ctx.font = this.style.contentFont.replace("{1}", (this.style.contentFontSize + this.style.contentFontUnit))
//         const metrics = ctx.measureText(this.content)
//
//         const labelWidth = metrics.width
//         const labelHeight = metrics.actualBoundingBoxDescent - metrics.actualBoundingBoxAscent
//         const hintWidth = (this.hintColor != null) ? (this.style.hintSize + this.style.hintMarginEnd) : 0
//         const hintHeight = (this.hintColor != null) ? this.style.hintSize : 0
//         this.updateSize(
//             labelWidth + hintWidth,
//             Math.max(labelHeight, hintHeight)
//         )
//
//         this.createHintPath()
//         this.hasMeasuredClientArea = true
//     }
//
//     protected createHintPath() {
//         this.hintPath = new Path2D()
//         const radius = this.style.hintSize
//         this.hintPath.ellipse(radius * 0.5, radius * 0.5, radius, radius, 0, 0, Math.PI * 2)
//         this.hintPath.closePath()
//     }
// }
//
// /**
//  * 带标题的标签
//  */
// class TitledLabel extends Label {
//
//     style = {
//         titleStyle: "#FFFFFF",
//         titleAlpha: 1,
//         titleFont: "bold {1}px HUD",
//         contentStyle: "#FFFFFF",
//         contentAlpha: 1,
//         contentFont: "bold {1}px HUD"
//     }
//
//     title: string
//     content: string
//     hintColor: string = ""
//
//     constructor(title: string, content: string, hintColor?: string) {
//         super();
//         this.title = title
//         this.content = content
//         this.hintColor = hintColor || ""
//     }
//
//     setPosition(x: number, y: number) {
//         super.setPosition(x, y);
//     }
//
//     setFontSize()
//
//     updateSize(width: number, height: number) {
//         super.updateSize(width, height);
//     }
//
//     draw(ctx: CanvasRenderingContext2D) {
//         super.draw(ctx);
//     }
//
// }