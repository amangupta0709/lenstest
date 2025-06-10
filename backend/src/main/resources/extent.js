// Toggle the visibility of test content when clicked
function toggleView(viewId) {
    const sections = document.querySelectorAll('.view');
    sections.forEach(section => {
        if (section.classList.contains(viewId)) {
            section.style.display = 'block';
        } else {
            section.style.display = 'none';
        }
    });
}
$(document).ready(function() {
    // Example to toggle visibility when clicking on dropdown
    $('.dropdown-toggle').on('click', function () {
        var $dropdownMenu = $(this).next('.dropdown-menu');
        $dropdownMenu.toggleClass('show');
    });

    // Example to collapse all tests
    $('.ct').on('click', function () {
        $('.test-contents').slideUp();
    });

    // Example to expand all tests
    $('.et').on('click', function () {
        $('.test-contents').slideDown();
    });

    // Toggle the test content visibility
    $('.test-item').on('click', function () {
        $(this).find('.test-contents').slideToggle();
    });
});