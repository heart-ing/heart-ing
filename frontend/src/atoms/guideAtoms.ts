import { atom } from "recoil";
import { IHeartDetailInfoTypes } from "../types/guideType";

export const openDetailInfoAtom = atom<boolean>({
  key: "openDetailInfo",
  default: false,
});

export const heartDetailInfoAtom = atom<IHeartDetailInfoTypes | null>({
  key: "heartDetailInfo",
  default: null,
});