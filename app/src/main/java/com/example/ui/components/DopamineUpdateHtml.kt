package com.example.ui.components

/**
 * 内置「多巴胺」风格更新弹窗模板（v1.8.0）
 * 纯 HTML/CSS/JS，由 AppUpdateDialog 通过 WebView 渲染。
 */
internal const val DOPAMINE_UPDATE_HTML = """<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<style>
*{box-sizing:border-box;-webkit-tap-highlight-color:transparent;margin:0;padding:0}
html,body{width:100%;height:100%;overflow:hidden;font-family:-apple-system,BlinkMacSystemFont,"Segoe UI","PingFang SC","Microsoft YaHei",Roboto,sans-serif;background:transparent;display:flex;align-items:center;justify-content:center}
.dopa-bg{position:fixed;inset:0;z-index:0;pointer-events:none;background:linear-gradient(125deg,#ff9a9e 0%,#fad0c4 18%,#fbc2eb 34%,#a18cd1 50%,#84fab0 66%,#8fd3f4 82%,#fad0c4 96%,#ff9a9e 100%);background-size:420% 420%;animation:dopaFlow 16s ease-in-out infinite;filter:saturate(1.35)}
@keyframes dopaFlow{0%{background-position:0% 50%}50%{background-position:100% 50%}100%{background-position:0% 50%}}
.blob{position:absolute;border-radius:50%;filter:blur(18px);opacity:.55}
.blob.b1{width:180px;height:180px;background:#ffd1ff;top:-40px;left:-30px;animation:float1 9s ease-in-out infinite}
.blob.b2{width:140px;height:140px;background:#a7f3d0;bottom:-30px;right:-20px;animation:float2 11s ease-in-out infinite}
.blob.b3{width:120px;height:120px;background:#bae6fd;top:40%;right:-40px;animation:float3 8s ease-in-out infinite}
.blob.b4{width:100px;height:100px;background:#fde68a;bottom:12%;left:-30px;animation:float1 10s ease-in-out infinite reverse}
@keyframes float1{0%,100%{transform:translate(0,0) scale(1)}50%{transform:translate(26px,18px) scale(1.12)}}
@keyframes float2{0%,100%{transform:translate(0,0) scale(1.05)}50%{transform:translate(-24px,-20px) scale(.92)}}
@keyframes float3{0%,100%{transform:translate(0,0)}50%{transform:translate(-16px,26px) scale(1.15)}}
.dots span{position:absolute;display:block;border-radius:50%;background:rgba(255,255,255,.5);animation:fall linear infinite}
.dots span:nth-child(1){width:8px;height:8px;left:8%;animation-duration:7s}
.dots span:nth-child(2){width:6px;height:6px;left:22%;animation-duration:9s;animation-delay:1.2s}
.dots span:nth-child(3){width:10px;height:10px;left:38%;animation-duration:8s;animation-delay:.5s}
.dots span:nth-child(4){width:5px;height:5px;left:55%;animation-duration:10s;animation-delay:2s}
.dots span:nth-child(5){width:9px;height:9px;left:70%;animation-duration:7.5s;animation-delay:.8s}
.dots span:nth-child(6){width:6px;height:6px;left:86%;animation-duration:9.5s;animation-delay:1.6s}
@keyframes fall{0%{transform:translateY(-30px);opacity:0}12%{opacity:.85}100%{transform:translateY(460px);opacity:0}}
.card{position:relative;z-index:2;width:330px;max-width:88vw;border-radius:28px;background:linear-gradient(160deg,rgba(255,255,255,.94),rgba(255,255,255,.82));-webkit-backdrop-filter:blur(16px);backdrop-filter:blur(16px);border:1.5px solid rgba(255,255,255,.9);box-shadow:0 22px 60px rgba(168,85,247,.35),0 6px 22px rgba(236,72,153,.22);padding:24px 22px 20px;text-align:center;transform-origin:center;animation:cardIn .55s cubic-bezier(.34,1.56,.64,1) both}
@keyframes cardIn{from{transform:scale(.72) translateY(22px);opacity:0}to{transform:scale(1) translateY(0);opacity:1}}
.rocket{width:76px;height:76px;margin:-56px auto 6px;border-radius:50%;background:radial-gradient(circle at 32% 30%,#fff3,transparent 45%),linear-gradient(135deg,#ff6ec7,#a855f7,#6366f1);display:flex;align-items:center;justify-content:center;font-size:36px;box-shadow:0 10px 26px rgba(168,85,247,.5),inset 0 0 18px rgba(255,255,255,.4);animation:rocketBob 2.4s ease-in-out infinite}
@keyframes rocketBob{0%,100%{transform:translateY(0) rotate(-4deg)}50%{transform:translateY(-7px) rotate(4deg)}}
.title{margin-top:10px;font-size:21px;font-weight:900;letter-spacing:1px;background:linear-gradient(90deg,#ff2d78,#a855f7,#6366f1,#ff2d78);background-size:300% 100%;-webkit-background-clip:text;background-clip:text;color:transparent;animation:titleFlow 4s linear infinite}
@keyframes titleFlow{to{background-position:300% 0}}
.ver{display:inline-block;margin-top:8px;padding:4px 14px;border-radius:999px;background:linear-gradient(90deg,#f472b6,#c084fc,#60a5fa);color:#fff;font-size:13px;font-weight:700;box-shadow:0 4px 12px rgba(192,132,252,.4)}
.logs{margin:14px 2px 0;text-align:left;max-height:118px;overflow-y:auto;-webkit-overflow-scrolling:touch}
.logs::-webkit-scrollbar{width:3px}
.logs::-webkit-scrollbar-thumb{background:#e9d5ff;border-radius:3px}
.log-item{display:flex;align-items:flex-start;gap:8px;padding:5px 0;font-size:12.5px;line-height:1.6;color:#475569;opacity:0;transform:translateX(-14px);animation:logIn .5s ease forwards}
.log-item i{flex:0 0 auto;width:7px;height:7px;margin-top:7px;border-radius:50%;background:linear-gradient(135deg,#ff6ec7,#a855f7);box-shadow:0 0 8px rgba(255,110,199,.8)}
.log-item b{font-weight:700;background:linear-gradient(90deg,#ff2d78,#a855f7,#6366f1,#ff2d78);background-size:260% 100%;-webkit-background-clip:text;background-clip:text;color:transparent;animation:logShine 3.2s linear infinite}
@keyframes logIn{to{opacity:1;transform:translateX(0)}}
@keyframes logShine{to{background-position:260% 0}}
.state{margin-top:14px}
.state .tip{font-size:13px;font-weight:800;letter-spacing:.5px;background:linear-gradient(90deg,#f97316,#ec4899,#8b5cf6,#ec4899,#f97316);background-size:300% 100%;-webkit-background-clip:text;background-clip:text;color:transparent;animation:tipFlow 2.6s linear infinite}
@keyframes tipFlow{to{background-position:300% 0}}
.state .tip-sub{margin-top:4px;font-size:10.5px;color:#94a3b8;animation:breathe 1.8s ease-in-out infinite}
@keyframes breathe{0%,100%{opacity:.55}50%{opacity:1}}
.bar{position:relative;height:15px;margin-top:12px;border-radius:999px;overflow:hidden;background:rgba(148,163,184,.18);box-shadow:inset 0 2px 5px rgba(100,116,139,.18)}
.bar .fill{position:absolute;inset:0;width:0%;background:linear-gradient(90deg,#ff6ec7,#facc15,#34d399,#38bdf8,#a855f7,#ff6ec7);background-size:300% 100%;border-radius:999px;transition:width .25s ease;animation:fillFlow 1.8s linear infinite;box-shadow:0 0 14px rgba(236,72,153,.75),0 0 26px rgba(168,85,247,.45)}
@keyframes fillFlow{to{background-position:300% 0}}
.bar .shine{position:absolute;top:0;bottom:0;width:46px;border-radius:999px;background:linear-gradient(90deg,transparent,rgba(255,255,255,.85),transparent);animation:shineSweep 1.3s linear infinite}
@keyframes shineSweep{from{left:-50px}to{left:105%}}
.pct{margin-top:8px;font-size:26px;font-weight:900;color:#8b5cf6}
.pct i{display:inline-block;animation:pctJump .9s ease-in-out infinite}
@keyframes pctJump{0%,100%{transform:translateY(0)}50%{transform:translateY(-4px)}}
.btns{margin-top:16px;display:flex;gap:10px}
.btn{flex:1;border:none;border-radius:999px;padding:13px 0;font-size:15px;font-weight:800;color:#fff;cursor:pointer;position:relative;overflow:hidden;outline:none}
.btn-primary{background:linear-gradient(135deg,#ff6ec7,#a855f7 55%,#6366f1);box-shadow:0 8px 22px rgba(168,85,247,.45);animation:btnPulse 1.6s ease-in-out infinite}
.btn-primary::after{content:"";position:absolute;top:0;bottom:0;width:40px;background:linear-gradient(90deg,transparent,rgba(255,255,255,.7),transparent);animation:btnSweep 2s linear infinite}
@keyframes btnSweep{from{left:-60px}to{left:110%}}
@keyframes btnPulse{0%,100%{transform:scale(1)}50%{transform:scale(1.035)}}
.btn-ghost{background:rgba(148,163,184,.22);color:#64748b}
.btn-green{background:linear-gradient(135deg,#34d399,#22d3ee);box-shadow:0 8px 22px rgba(52,211,153,.4)}
.btn-orange{background:linear-gradient(135deg,#fb923c,#f43f5e);box-shadow:0 8px 22px rgba(244,63,94,.4)}
.group-link{margin-top:12px;font-size:12px;color:#8b5cf6;font-weight:700;cursor:pointer;text-decoration:underline;text-underline-offset:3px}
.spinner{width:66px;height:66px;margin:6px auto 8px;border-radius:50%;border:5px solid rgba(168,85,247,.16);border-top-color:#a855f7;animation:spin 1s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}
.done-emoji{font-size:44px;animation:doneBounce 1s ease infinite}
@keyframes doneBounce{0%,100%{transform:scale(1)}50%{transform:scale(1.18)}}
.err-msg{margin-top:10px;font-size:12.5px;color:#e11d48;line-height:1.6;word-break:break-all}
</style>
</head>
<body>
<div class="dopa-bg"><div class="blob b1"></div><div class="blob b2"></div><div class="blob b3"></div><div class="blob b4"></div></div>
<div class="dots"><span></span><span></span><span></span><span></span><span></span><span></span></div>

<div class="card">
  <div class="rocket">🚀</div>
  <div class="title" id="dTitle">发现新版本</div>
  <div class="ver" id="dVer">v2.0.0</div>
  <div class="logs" id="dLogs"></div>
  <div class="state">
    <div class="tip" id="dTip">优化使用体验，修复已知问题</div>
    <div class="tip-sub" id="dSub"></div>
    <div class="bar" id="dBar" style="display:none"><div class="fill" id="dFill"></div><div class="shine"></div></div>
    <div class="pct" id="dPct" style="display:none"><i>0</i>%</div>
  </div>
  <div class="btns" id="dBtns"></div>
  <div class="group-link" id="dGroup" style="display:none">💬 官方群 · 遇到问题来反馈</div>
</div>

<script>
"use strict";
var q=function(id){return document.getElementById(id)};
var defaultLogs=[
  "叮咚～我们又又又更新啦！",
  "赶紧快来看看新版本有什么好宝贝吧",
  "我们一直在白嫖的路上，一直在奔跑哟",
  "快点更新吧～期待您发现自己的新大陆。"
];
function esc(s){return String(s==null?"":s).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;")}
function renderLogs(list){
  var box=q("dLogs");box.innerHTML="";
  (list&&list.length?list:defaultLogs).forEach(function(t,i){
    var d=document.createElement("div");d.className="log-item";
    d.style.animationDelay=(i*0.09)+"s";
    d.innerHTML="<i></i><span>"+esc(t)+"</span>";
    box.appendChild(d);
  });
}
function showTip(t){q("dTip").textContent=t||"";q("dTip").style.display="";}
function hideBar(){q("dBar").style.display="none";q("dPct").style.display="none";}
function setBar(p){p=Math.max(0,Math.min(100,p));q("dBar").style.display="";q("dPct").style.display="";q("dFill").style.width=p+"%";q("dPct").innerHTML="<i>"+Math.round(p)+"</i>%";}
function mkBtn(text,cls,fn){var b=document.createElement("button");b.className="btn "+cls;b.textContent=text;b.onclick=fn;return b;}
var phase="found", force=false;
function setState(s){
  try{
    if(!s) return;
    if(s.title)q("dTitle").textContent=s.title;
    if(s.ver)q("dVer").textContent="v"+s.ver;
    if(s.logs)renderLogs(s.logs);
    force=!!s.force;
    phase=s.phase||"found";
    q("dGroup").style.display="none";
    var B=q("dBtns");B.innerHTML="";
    hideBar();showTip("优化使用体验，修复已知问题");
    if(s.tip)showTip(s.tip);
    if(s.sub)q("dSub").textContent=s.sub;
    if(phase==="downloading"){
      setBar(s.progress||0);
      q("dGroup").style.display=force?"":"none";
    }else if(phase==="installing"){
      showTip("正在安装新版本…");q("dSub").textContent="安装完成即可体验全新功能 ✨";
      var sp=document.createElement("div");sp.className="spinner";q("dState").insertBefore(sp,q("dBar"));
      window._sp=sp;
    }else if(phase==="done"){
      if(window._sp&&window._sp.parentNode)window._sp.parentNode.removeChild(window._sp);window._sp=null;
      showTip("🎉 更新完成！");
      q("dSub").textContent="新版本已安装成功，重启应用即可体验全新功能 ✨";
      var em=document.createElement("div");em.className="done-emoji";em.textContent="🎊";
      q("dState").insertBefore(em,q("dBar"));
      B.appendChild(mkBtn("立即重启","btn-green",function(){try{AndroidBridge.restart()}catch(e){}}));
      B.appendChild(mkBtn("稍后再说","btn-ghost",function(){try{AndroidBridge.close()}catch(e){}}));
      return;
    }else if(phase==="error"){
      if(window._sp&&window._sp.parentNode)window._sp.parentNode.removeChild(window._sp);window._sp=null;
      showTip("下载失败，请重试");
      if(s.msg)q("dSub").textContent=s.msg;
      B.appendChild(mkBtn("重试","btn-orange",function(){try{AndroidBridge.download()}catch(e){}}));
      if(!force)B.appendChild(mkBtn("稍后再说","btn-ghost",function(){try{AndroidBridge.close()}catch(e){}}));
      return;
    }else if(phase==="permission"){
      if(window._sp&&window._sp.parentNode)window._sp.parentNode.removeChild(window._sp);window._sp=null;
      showTip("需要开启「允许安装未知应用」");
      q("dSub").textContent="开启权限后回到这里，点下方按钮继续自动安装";
      B.appendChild(mkBtn("去开启权限","btn-primary",function(){try{AndroidBridge.openPermission()}catch(e){}}));
      if(!force)B.appendChild(mkBtn("暂不更新","btn-ghost",function(){try{AndroidBridge.close()}catch(e){}}));
      return;
    }else{
      // found
      showTip("优化使用体验，修复已知问题");
      q("dSub").textContent="";
      if(!force)B.appendChild(mkBtn("稍后再说","btn-ghost",function(){try{AndroidBridge.close()}catch(e){}}));
      B.appendChild(mkBtn("立即更新","btn-primary",function(){try{AndroidBridge.download()}catch(e){}}));
      q("dGroup").style.display=force?"":"";
    }
  }catch(e){}
}
// 初始化：官方群链接点击 → AndroidBridge.openGroup()
q("dGroup").addEventListener("click",function(){try{AndroidBridge.openGroup()}catch(e){}});
window.setState=setState;
</script>
</body>
</html>"""
