import { axios } from "./https";
import { IMessageSendTypes } from "../../types/messageType";

export async function getReceived(userId: string) {
  try {
    const res = await axios.get(`api/v1/messages/received/${userId}`);
    const data = res.data;
    return data;
  } catch (err) {
    console.log("24시간 내 받은 메시지 조회에 실패했습니다.");
    console.log(err);
    return null;
  }
}

export async function sendMessage(body: IMessageSendTypes) {
  try {
    const res = await axios.post(`api/v1/messages`, body);
    const data = res.data;
    return data;
  } catch (err) {
    console.log(err);
    return null;
  }
}

export async function getMessageDetail(messageId: number) {
  try {
    const res = await axios.get(`api/v1/messages/received/detail/${messageId}`);
    const data = res.data;
    return data;
  } catch (err) {
    console.log("상세 메시지 보기에 실패했습니다");
    console.log(err);
    return null;
  }
}
