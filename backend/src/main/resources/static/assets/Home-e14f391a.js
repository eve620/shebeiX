import{_ as N,g as m,h as o,b as h,c as k,d as n,t as C,i as e,j as u,m as I,r as b,p as T,f as U,o as j,a as O,u as A,w as S,k as $,n as F,F as H,l as W,q as E,s as J,x as P,y as M}from"./index-03f9d86f.js";import{g as V}from"./Instance-e9cab15d.js";const G="/logo-sider.png";const Q=a=>(T("data-v-334d2762"),a=a(),U(),a),X={class:"top-bar"},Y={class:"top-bar-info"},Z=Q(()=>n("span",{style:{"padding-left":"2px"}},"退出",-1)),ee={style:{"padding-left":"3px","font-weight":"bold"}},te={__name:"TopBar",props:{userInfo:{type:Object,required:!0},title:{type:String,required:!0}},setup(a){const f=a,p=V(),l=m(""),i=m(!1),g=()=>{i.value=!0},_=()=>{p.changeUserPassword(f.userInfo.userId,l.value).then(c=>{c.data.code===1?I.info(c.data.data):I.info(c.data.msg)}),l.value="",i.value=!1},x=()=>{l.value="",i.value=!1},y=()=>{p.logoutApi().then(c=>{c.data.code===1&&b.push("/login")})};return(c,d)=>{const t=o("a-input"),s=o("a-modal"),w=o("LogoutOutlined");return h(),k("div",X,[n("div",null,C(f.title),1),e(s,{open:i.value,"onUpdate:open":d[1]||(d[1]=v=>i.value=v),"ok-text":"确定","cancel-text":"取消",onOk:_,onCancel:x,title:"修改密码"},{default:u(()=>[e(t,{id:"password",value:l.value,"onUpdate:value":d[0]||(d[0]=v=>l.value=v),placeHolder:"新密码"},null,8,["value"])]),_:1},8,["open"]),n("div",Y,[n("div",{onClick:g},"修改密码"),n("div",{onClick:y},[e(w,{onClick:y}),Z]),n("span",ee,C(f.userInfo.userName),1)])])}}},oe=N(te,[["__scopeId","data-v-334d2762"]]);const ne=a=>(T("data-v-58c6e3ba"),a=a(),U(),a),ae={class:"loading"},se=ne(()=>n("div",{class:"logo"},[n("img",{src:G,style:{padding:"30px 8px 30px 8px",width:"100%"},alt:"logo"})],-1)),le={class:"nav-text"},ie={style:{padding:"24px",background:"#fff",minHeight:"360px"}},de={__name:"Home",setup(a){const f=V();j(()=>{f.whoami().then(t=>{t.data.code===1?(p=t.data.data,l.value=p.roleId,i.value=1,l.value&&_.push({id:"3",name:"教师管理",icon:"UserOutlined",url:"/home/user"},{id:"4",name:"管理员管理",icon:"ToolOutlined",url:"/home/manager"}),d.value=_.find(s=>g.value.startsWith(s.url))?[_.find(s=>g.value.startsWith(s.url)).id]:["1"]):(I.info(t.data.msg),b.push("/login"))})});let p=O({userId:void 0,roleId:void 0,userJobnumber:void 0,userName:void 0});const l=m(),i=m(0),g=m(A().path),_=O([{id:"1",name:"资产管理",icon:"PayCircleOutlined",url:"/home/item"},{id:"2",name:"实验室管理",icon:"HomeOutlined",url:"/home/lab"}]),x=t=>{b.push(t)},y=(t,s)=>{console.log(t,s)},c=t=>{console.log(t)},d=m(["1"]);return(t,s)=>{const w=o("a-spin"),v=o("a-menu-item"),K=o("a-menu"),L=o("a-layout-sider"),R=o("a-layout-header"),q=o("RouterView"),z=o("a-layout-content"),D=o("a-layout-footer"),B=o("a-layout");return h(),k(H,null,[S(n("div",ae,[e(w,{size:"large"})],512),[[$,!i.value]]),S(e(B,{style:{height:"auto","min-height":"100vh"},class:F({themeRed:!l.value})},{default:u(()=>[e(L,{breakpoint:"lg","collapsed-width":"0",onCollapse:y,onBreakpoint:c},{default:u(()=>[se,e(K,{selectedKeys:d.value,"onUpdate:selectedKeys":s[0]||(s[0]=r=>d.value=r),theme:"dark",mode:"inline"},{default:u(()=>[(h(!0),k(H,null,W(_,r=>(h(),P(v,{key:r.id,onClick:ue=>x(r.url)},{default:u(()=>[(h(),P(M(r.icon),{style:{"font-size":"16px","line-height":"40px"}})),n("span",le,C(r.name),1)]),_:2},1032,["onClick"]))),128))]),_:1},8,["selectedKeys"])]),_:1}),e(B,{style:{"min-width":"1000px"}},{default:u(()=>[e(R,{style:{background:"#fff"}},{default:u(()=>[e(oe,{userInfo:E(p),title:_.find(r=>r.id===d.value[0]).name},null,8,["userInfo","title"])]),_:1}),e(z,{style:{margin:"24px 16px 0"}},{default:u(()=>[n("div",ie,[e(q)])]),_:1}),e(D,{style:{"text-align":"center"}},{default:u(()=>[J(" 开发维护：西安电子科技大学电子工程学院 ")]),_:1})]),_:1})]),_:1},8,["class"]),[[$,i.value]])],64)}}},_e=N(de,[["__scopeId","data-v-58c6e3ba"]]);export{_e as default};