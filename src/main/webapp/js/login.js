// login.js

document.getElementById('loginForm')?.addEventListener('submit', function(e) {
  e.preventDefault();

  const username = document.getElementById('username')?.value.trim();
  const password = document.getElementById('password')?.value.trim();
  const errorMsg = document.getElementById('error-msg');

  if(username && password) {
    // Admin check: username 'admin' and password '1234'
    if(username.toLowerCase() === 'admin' && password === '1234') {
      sessionStorage.setItem('isAdmin', 'true');
    } else {
      sessionStorage.setItem('isAdmin', 'false');
    }

    // Store username for session
    sessionStorage.setItem('username', username);

    // Redirect back to index or rooms page
    window.location.href = 'index.html';
  } else {
    errorMsg.textContent = 'Please enter username and password';
  }
});
