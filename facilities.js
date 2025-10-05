document.addEventListener('DOMContentLoaded', () => {
    const buttons = document.querySelectorAll('.toggle-info');

    buttons.forEach(button => {
        const originalText = button.textContent; 

        button.addEventListener('click', () => {
            const details = button.nextElementSibling;

            if (details.style.display === "none") {
                details.style.display = "block";
                button.textContent = `Show Less`;
            } else {
                details.style.display = "none";
                button.textContent = originalText;
            }
        });
    });
});

