function r(e,i,{confirmText:l="确认",cancelText:c="取消"}={}){return new Promise(d=>{const n=document.createElement("div");n.className="modal-overlay";const t=document.createElement("div");t.className="modal-confirm",t.innerHTML=`
      <div class="modal-header">${e}</div>
      <div class="modal-body">${i}</div>
      <div class="modal-footer">
        <button class="btn-pill btn-cancel">${c}</button>
        <button class="btn-pill btn-pill-primary btn-confirm">${l}</button>
      </div>
    `,n.appendChild(t),document.body.appendChild(n);const a=o=>{n.remove(),d(o)};if(t.querySelector(".btn-cancel").onclick=()=>a(!1),t.querySelector(".btn-confirm").onclick=()=>a(!0),n.onclick=o=>{o.target===n&&a(!1)},!document.getElementById("modal-styles")){const o=document.createElement("style");o.id="modal-styles",o.textContent=`
        .modal-overlay {
          position: fixed; inset: 0; background: rgba(0,0,0,0.4);
          display: flex; align-items: center; justify-content: center;
          z-index: 10000; animation: fadeIn 0.2s ease;
        }
        .modal-confirm {
          background: #fff; border-radius: 14px; padding: 24px;
          max-width: 400px; width: 90%; box-shadow: 0 16px 48px rgba(0,0,0,0.15);
          animation: scaleIn 0.2s ease;
        }
        .modal-header { font-size: 17px; font-weight: 600; color: #333; margin-bottom: 10px; }
        .modal-body { font-size: 14px; color: #666; margin-bottom: 24px; line-height: 1.6; }
        .modal-footer { display: flex; gap: 10px; justify-content: flex-end; }
        .btn-cancel {
          background: #f5f5f5; color: #666; border-radius: 50px;
          padding: 8px 20px; font-size: 14px; border: none; cursor: pointer;
        }
        .btn-cancel:hover { background: #eee; }
        .btn-confirm { padding: 8px 20px; font-size: 14px; }
        @keyframes fadeIn { from { opacity: 0 } to { opacity: 1 } }
        @keyframes scaleIn { from { transform: scale(0.95); opacity: 0 } to { transform: scale(1); opacity: 1 } }
      `,document.head.appendChild(o)}})}const s={confirm(e){return typeof e=="string"?r("提示",e):r(e.title||"提示",e.message,e)}};export{s as D};
