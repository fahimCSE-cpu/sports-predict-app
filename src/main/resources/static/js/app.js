(function () {
  // Welcome frame: appears suddenly shortly after the page opens (once per browser session).
  var welcome = document.getElementById('welcome');
  if (welcome) {
    var seen = false;
    try { seen = sessionStorage.getItem('welcomeSeen') === '1'; } catch (e) {}
    function close() {
      welcome.hidden = true;
      try { sessionStorage.setItem('welcomeSeen', '1'); } catch (e) {}
    }
    if (!seen) setTimeout(function () { welcome.hidden = false; var b = document.getElementById('welcome-close'); if (b) b.focus(); }, 700);
    document.getElementById('welcome-close').addEventListener('click', close);
    welcome.addEventListener('click', function (e) { if (e.target === welcome) close(); });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape' && !welcome.hidden) close(); });
  }

  // "Check which team succeeds?" reveals the prediction and logs the click for analytics.
  var tokenEl = document.querySelector('meta[name=_csrf]');
  var headerEl = document.querySelector('meta[name=_csrf_header]');
  document.querySelectorAll('.check-btn').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var result = btn.closest('.banner').querySelector('.result');
      var opening = result.hidden;
      result.hidden = !opening;
      btn.textContent = opening ? 'Hide prediction' : 'Check which team succeeds?';
      if (opening) {
        var headers = {};
        if (tokenEl && headerEl) headers[headerEl.content] = tokenEl.content;
        fetch('/api/match/' + btn.dataset.match + '/check', { method: 'POST', headers: headers }).catch(function () {});
      }
    });
  });
})();
