var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        if (typeof b !== "function" && b !== null)
            throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
define("HUD", ["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    exports.DrawingObject = exports.DrawingDriver = exports.Math2D = exports.Rect = void 0;
    var Rect = (function () {
        function Rect(left, top, right, bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
        Rect.prototype.x = function () { return this.left; };
        Rect.prototype.y = function () { return this.top; };
        Rect.prototype.width = function () { return (this.right - this.left); };
        Rect.prototype.height = function () { return (this.bottom - this.top); };
        Rect.prototype.centerX = function () { return (this.left + this.width() * 0.5); };
        Rect.prototype.centerY = function () { return (this.top + this.height() * 0.5); };
        return Rect;
    }());
    exports.Rect = Rect;
    var Math2D = (function () {
        function Math2D() {
        }
        Math2D.toDegree = function (radius) {
            return 180.0 * radius / Math.PI;
        };
        Math2D.toRadius = function (degrees) {
            return degrees * Math.PI / 180;
        };
        return Math2D;
    }());
    exports.Math2D = Math2D;
    var DrawingDriver = (function () {
        function DrawingDriver(canvas) {
            this.hasInvalidate = false;
            this.hasResize = false;
            this.clientWidth = 0;
            this.clientHeight = 0;
            this.canvas = canvas;
            this.ctx = canvas.getContext("2d");
            canvas.addEventListener("onresize", this.resize);
            canvas.addEventListener("oninvalid", this.invalidate);
        }
        DrawingDriver.prototype.drawInner = function () {
            if (this.needResize()) {
                this.requestResize();
            }
            this.hasInvalidate = false;
            if (this.ctx) {
                this.onDraw(this.ctx);
            }
        };
        DrawingDriver.prototype.needResize = function () {
            return (this.canvas.clientWidth != this.clientWidth) || (this.canvas.clientHeight != this.clientHeight);
        };
        DrawingDriver.prototype.resizeInner = function () {
            this.hasResize = false;
            if (this.canvas) {
                if ((this.canvas.clientWidth != this.clientWidth) || (this.canvas.clientHeight != this.clientHeight)) {
                    this.clientWidth = this.canvas.clientWidth;
                    this.clientHeight = this.canvas.clientHeight;
                    this.canvas.width = this.clientWidth;
                    this.canvas.height = this.clientHeight;
                    console.log("[resizeInner] " + this.canvas.width + "x" + this.canvas.height);
                    this.onResize(this.clientWidth, this.clientHeight);
                }
            }
        };
        DrawingDriver.prototype.requestResize = function () {
            if (this.hasResize)
                return;
            var instance = this;
            requestAnimationFrame(function () {
                instance.resizeInner();
                instance.drawInner();
            });
        };
        DrawingDriver.prototype.requestRender = function () {
            if (this.hasInvalidate)
                return;
            var instance = this;
            requestAnimationFrame(function () {
                instance.drawInner();
            });
        };
        DrawingDriver.prototype.resize = function (event) {
            this.requestResize();
        };
        DrawingDriver.prototype.invalidate = function (event) {
            this.requestRender();
        };
        DrawingDriver.prototype.onResize = function (width, height) { };
        DrawingDriver.prototype.onDraw = function (ctx) { };
        return DrawingDriver;
    }());
    exports.DrawingDriver = DrawingDriver;
    var DrawingObject = (function () {
        function DrawingObject() {
            this.clientArea = new Rect(0, 0, DrawingObject.AUTO_SIZE, DrawingObject.AUTO_SIZE);
            this.root = null;
            this.clipToPadding = false;
        }
        DrawingObject.prototype.attachToRoot = function (root) {
            this.root = root;
        };
        DrawingObject.prototype.setPosition = function (x, y) {
            var offsetX = x - this.clientArea.left;
            var offsetY = y - this.clientArea.top;
            this.clientArea.left += offsetX;
            this.clientArea.right += offsetX;
            this.clientArea.top += offsetY;
            this.clientArea.bottom += offsetY;
        };
        DrawingObject.prototype.updateSize = function (width, height) {
            this.clientArea.right = this.clientArea.left + width;
            this.clientArea.bottom = this.clientArea.top + height;
        };
        DrawingObject.prototype.draw = function (ctx) {
            ctx.save();
            if (this.clipToPadding) {
                ctx.rect(this.clientArea.left, this.clientArea.top, this.clientArea.width(), this.clientArea.height());
                ctx.clip();
            }
            ctx.translate(this.clientArea.left, this.clientArea.top);
            this.onDraw(ctx);
            ctx.restore();
        };
        DrawingObject.prototype.onDraw = function (ctx) { };
        DrawingObject.prototype.invalidate = function () {
            if (this.root) {
                this.root.requestRender();
            }
        };
        DrawingObject.AUTO_SIZE = -1;
        return DrawingObject;
    }());
    exports.DrawingObject = DrawingObject;
});
define("CurveMeter", ["require", "exports", "HUD"], function (require, exports, HUD_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    exports.ScrollableCurveMeter = exports.CurveMeter = void 0;
    var CurveMeter = (function (_super) {
        __extends(CurveMeter, _super);
        function CurveMeter() {
            var _this = _super !== null && _super.apply(this, arguments) || this;
            _this.style = {
                bg: {
                    grid: {
                        lineColor: "#FFFFFF",
                        lineAlpha: 0.1,
                        lineWidth: 1,
                        xRangeGrids: 4,
                        yRangeGrids: 4
                    }
                }
            };
            _this.gridPath = null;
            _this.gridChangeMask = "";
            return _this;
        }
        CurveMeter.prototype.onDraw = function (ctx) {
            _super.prototype.onDraw.call(this, ctx);
            this.drawGrid(ctx);
            this.drawCurve(ctx);
        };
        CurveMeter.prototype.calcGridChangeMask = function () {
            return "" + this.style.bg.grid.xRangeGrids + this.style.bg.grid.yRangeGrids
                + this.clientArea.width() + this.clientArea.height();
        };
        CurveMeter.prototype.buildGridPath = function () {
            var lineWidthOffset = Math.ceil(this.style.bg.grid.lineWidth * 0.5);
            var left = this.clientArea.left + lineWidthOffset;
            var right = this.clientArea.right - lineWidthOffset;
            var top = this.clientArea.top + lineWidthOffset;
            var bottom = this.clientArea.bottom - lineWidthOffset;
            var grid = new Path2D();
            grid.rect(left, top, right - left, bottom - top);
            var lineLeft = this.clientArea.left + this.style.bg.grid.lineWidth;
            var lineRight = this.clientArea.right - this.style.bg.grid.lineWidth;
            var lineTop = this.clientArea.top + this.style.bg.grid.lineWidth;
            var lineBottom = this.clientArea.bottom - this.style.bg.grid.lineWidth;
            var verticalStep = (bottom - top) / Math.max(1, this.style.bg.grid.xRangeGrids);
            var horizontalStep = (right - left) / Math.max(1, this.style.bg.grid.yRangeGrids);
            for (var i = 1; i < this.style.bg.grid.yRangeGrids; i++) {
                grid.moveTo(lineLeft, lineTop + horizontalStep * i);
                grid.lineTo(lineRight, lineTop + horizontalStep * i);
            }
            for (var i = 1; i < this.style.bg.grid.xRangeGrids; i++) {
                grid.moveTo(lineLeft + verticalStep * i, lineTop);
                grid.lineTo(lineLeft + verticalStep * i, lineBottom);
            }
        };
        CurveMeter.prototype.drawGrid = function (ctx) {
            var currentChangeMask = this.calcGridChangeMask();
            if (currentChangeMask != this.gridChangeMask) {
                this.gridChangeMask = currentChangeMask;
                this.buildGridPath();
            }
            if (this.gridPath) {
                ctx.strokeStyle = this.style.bg.grid.lineColor;
                ctx.globalAlpha = this.style.bg.grid.lineAlpha;
                ctx.lineWidth = this.style.bg.grid.lineWidth;
                ctx.lineCap = "butt";
                ctx.stroke(this.gridPath);
            }
        };
        CurveMeter.prototype.drawCurve = function (ctx) {
        };
        CurveMeter.prototype.getStartBound = function () {
            return 0;
        };
        CurveMeter.prototype.getEndBound = function () {
            return 0;
        };
        CurveMeter.prototype.getTopBound = function () {
            return 0;
        };
        CurveMeter.prototype.getBottomBound = function () {
            return 0;
        };
        return CurveMeter;
    }(HUD_1.DrawingObject));
    exports.CurveMeter = CurveMeter;
    var ScrollableCurveMeter = (function (_super) {
        __extends(ScrollableCurveMeter, _super);
        function ScrollableCurveMeter() {
            return _super.call(this) || this;
        }
        return ScrollableCurveMeter;
    }(CurveMeter));
    exports.ScrollableCurveMeter = ScrollableCurveMeter;
});
