document.getElementById('loginFormModal').addEventListener('submit', function(e) {
  e.preventDefault();

  const username = document.getElementById('usernameModal').value.trim();
  const password = document.getElementById('passwordModal').value.trim();
  const errorMsg = document.getElementById('error-msg-modal');

  if(username && password) {
    // Detect if admin
    if(username.toLowerCase() === 'admin') {
      sessionStorage.setItem('isAdmin', 'true');
    } else {
      sessionStorage.setItem('isAdmin', 'false');
    }

    // Store username
    sessionStorage.setItem('username', username);

    // Close modal
    const loginModal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
    loginModal.hide();

    // Redirect to booking page
    window.location.href = 'index.html';
  } else {
    errorMsg.textContent = 'Please enter a username and password';
  }
});
