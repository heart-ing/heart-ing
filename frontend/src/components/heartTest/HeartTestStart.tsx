import React from 'react'
import LogoEffect from "../../assets/images/logo/logo_effect.png";

function HeartTestStart({...props}) {
  return (
    <div className="container mx-auto px-6 fullHeight">
      <img src={LogoEffect} alt="test" className="w-full px-14 py-8 mt-12" />
      <div className="text-4xl py-3 textShadow">
        <p className="text-hrtColorYellow">하트테스트</p>
      </div>
      <p className='whitespace-pre-wrap mt-4 mb-16'>{`당신 마음 속의\n심볼 하트를 찾아보세요\n♥`}</p>
      <div onClick={props.onTestModeHanlder} className='mx-auto h-12 w-40 flex justify-center items-center rounded-xl border-2 bg-hrtColorYellow border-hrtColorPink shadow-[0_4px_4px_rgba(251,139,176,1)]'>
          <p>시작</p>        
      </div>
    </div>
  )
}

export default HeartTestStart
