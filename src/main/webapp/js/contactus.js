// Grab elements
const form = document.getElementById('contactForm');
const nameInput = document.getElementById('nameInput');
const emailInput = document.getElementById('emailInput');
const topicSelect = document.getElementById('topicSelect');
const messageInput = document.getElementById('messageInput');
const consentChk = document.getElementById('consentChk');
const submitBtn = document.getElementById('submitBtn');
const alertSpot = document.getElementById('alertSpot');

// Simple email check (enough for classwork)
function validEmail(v) {
  return /\S+@\S+\.\S+/.test(v);
}

// Show a Bootstrap-style alert
function showAlert(type, msg) {
  // type: 'success' | 'danger' | 'warning' | 'info'
  alertSpot.innerHTML = `
    <div class="alert alert-${type} alert-dismissible fade show" role="alert">
      ${msg}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>`;
}

// Live validation styles using Bootstrap classes
function markValidity(input, ok) {
  input.classList.toggle('is-invalid', !ok);
  input.classList.toggle('is-valid', ok);
}

// Enable/disable the submit button based on consent
function updateSubmitState() {
  submitBtn.disabled = !consentChk.checked;
}

// Live checks
nameInput.addEventListener('input', () => markValidity(nameInput, nameInput.value.trim().length > 0));
emailInput.addEventListener('input', () => markValidity(emailInput, validEmail(emailInput.value.trim())));
messageInput.addEventListener('input', () => markValidity(messageInput, messageInput.value.trim().length > 0));
consentChk.addEventListener('change', updateSubmitState);

// Initial state
updateSubmitState();

// Submit handler
form.addEventListener('submit', (e) => {
  e.preventDefault();

  const name = nameInput.value.trim();
  const email = emailInput.value.trim();
  const topic = topicSelect.value;
  const message = messageInput.value.trim();

  // Validate minimal fields
  const okName = name.length > 0;
  const okEmail = validEmail(email);
  const okMsg = message.length > 0;
  markValidity(nameInput, okName);
  markValidity(emailInput, okEmail);
  markValidity(messageInput, okMsg);

  if (!okName || !okEmail || !okMsg || !consentChk.checked) {
    showAlert('danger', 'Please fill all fields correctly and agree to be contacted.');
    return;
  }

  // Simulate sending UX
  submitBtn.disabled = true;
  const oldText = submitBtn.textContent;
  submitBtn.textContent = 'Sending...';

  // Fake small delay for nicer feel
  setTimeout(() => {
    showAlert('success', `Thank you, ${name}! We received your ${topic.toLowerCase()} inquiry.`);
    form.reset();
    // Clear validation styles after reset
    [nameInput, emailInput, messageInput].forEach(el => {
      el.classList.remove('is-valid', 'is-invalid');
    });
    updateSubmitState();
    submitBtn.textContent = oldText;
  }, 600);
});