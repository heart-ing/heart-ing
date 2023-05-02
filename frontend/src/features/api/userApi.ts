import { axios, nonAuthAxios } from './https'

export async function login(provider: string, code: string) {
  try{
    const res = await nonAuthAxios.get(`api/v1/auth/guests/social/${provider}?code=${code}`);
    const data = res.data
    return data
  } catch(err) {
    console.log('소셜 안됐단다')
    console.log(err)
    return null
  }
}

export async function modifyNickname(nickname: object) {
  try{
    const res = await axios.patch('api/v1/auth/users/nickname', nickname)
    const status = res.data.status
    return status
  } catch(err) {
    console.log('닉넴 못바꿧어용')
    return err
  }
}

export async function modifyStatusMessage(statusMessage: object) {
  try{
    const res = await axios.patch('api/v1/auth/users/status-message', statusMessage)
    const status = res.data.status
    return status
  } catch(err) {
    console.log('상메 못바꿧어용')
    return err
  }
}

export async function logout() {
  try{
    const res = await axios.patch('api/v1/auth/users/logout')
    const status = res.data.status
    return status
  } catch(err) {
    console.log('로그아웃 안됐단다')
    return null
  }
}

export async function getProfile(userId:string) {
  try{
    const res = await nonAuthAxios.get(`api/v1/auth/guests/${userId}`)
    const data = res.data
    return data
  } catch(err) {
    console.log('없는 유저')
    return err
  }
}

export async function reissueTokenApi() {
  try{
    const res = await nonAuthAxios.get('api/v1/auth/users/access-token')
    const data = res.data
    return data
  } catch(err) {
    console.log('쫓아낸다ㅋ')
    return err
  }
}