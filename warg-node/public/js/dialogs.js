// warg - small modal dialog helper. GPLv3.

export function el(tag, attrs = {}, ...children) {
  const e = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (v == null || v === false) continue;
    if (k === 'class') e.className = v;
    else if (k === 'text') e.textContent = v;
    else if (k.startsWith('on')) e.addEventListener(k.substring(2), v);
    else if (k === 'style' && typeof v === 'object') Object.assign(e.style, v);
    else e.setAttribute(k, v === true ? '' : v);
  }
  for (const c of children.flat()) {
    if (c == null || c === false) continue;
    e.append(c instanceof Node ? c : document.createTextNode(String(c)));
  }
  return e;
}

const root = () => document.getElementById('modal-root');

/**
 * opens a dialog. actions: [{label, primary, danger, onClick, id, disabled}]
 * passive dialogs do not block the map (battle dialog when only watching)
 */
export function openDialog({ title, body, actions = [], wide = false, passive = false, onClose = null, closeOnEscape = true }) {
  const actionBar = el('div', { class: 'actions' });
  const dialog = el('div', { class: 'dialog' + (wide ? ' wide' : ''), role: 'dialog', 'aria-modal': passive ? 'false' : 'true', 'aria-label': title || '' },
    title ? el('h2', { text: title }) : null,
    el('div', { class: 'body' }, body),
    actionBar);
  const back = el('div', { class: 'modal-back' + (passive ? ' passive' : '') }, dialog);
  const api = {
    el: dialog,
    back,
    buttons: {},
    closed: false,
    close() {
      if (api.closed) return;
      api.closed = true;
      back.remove();
      document.removeEventListener('keydown', onKey);
      if (onClose) onClose();
    },
    setActions(list) {
      actionBar.replaceChildren();
      api.buttons = {};
      for (const a of list) {
        if (a.node) { actionBar.append(a.node); continue; }
        const b = el('button', {
          class: 'btn' + (a.primary ? ' primary' : '') + (a.danger ? ' danger' : '') + (a.ghost ? ' ghost' : ''),
          type: 'button',
          disabled: a.disabled,
          onclick: () => a.onClick && a.onClick(api),
        }, a.label);
        if (a.id) api.buttons[a.id] = b;
        actionBar.append(b);
      }
      actionBar.hidden = list.length === 0;
    },
  };
  const onKey = (e) => {
    if (e.key === 'Escape' && closeOnEscape) api.close();
  };
  document.addEventListener('keydown', onKey);
  api.setActions(actions);
  root().append(back);
  const focusable = dialog.querySelector('input, button.primary, button');
  if (focusable && !passive) setTimeout(() => focusable.focus(), 0);
  return api;
}

export function alertDialog(title, text, okLabel = 'OK') {
  return new Promise((resolve) => {
    openDialog({
      title,
      body: el('p', { text }),
      actions: [{ label: okLabel, primary: true, onClick: (d) => d.close() }],
      onClose: resolve,
    });
  });
}

export function confirmDialog(title, text, yes, no) {
  return new Promise((resolve) => {
    let answer = false;
    openDialog({
      title,
      body: el('p', { text }),
      actions: [
        { label: no, ghost: true, onClick: (d) => d.close() },
        { label: yes, danger: true, onClick: (d) => { answer = true; d.close(); } },
      ],
      onClose: () => resolve(answer),
    });
  });
}
