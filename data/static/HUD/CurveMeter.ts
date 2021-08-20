import {
    DrawingObject
} from "./HUD";

export interface SlidingWindowAdapter<T> {
    getDataCount(): T
    getVisibleRange(): [min: T, max: T]
    getData(position: number): T
}

export class CurveMeter extends DrawingObject {

    onDraw(ctx: CanvasRenderingContext2D) {
        super.onDraw(ctx);
    }
}

export class ScrollableCurveMeter extends CurveMeter {

}