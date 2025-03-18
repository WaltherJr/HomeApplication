

$('body').on('click', 'button[aria-label="Mina sidor"]', function() {
    $('div[data-testid="product-container"]:not(.selected-grocery)').addClass('grocery-hidden');
});

$('body').on('click', 'div[data-testid="product-container"]', function() {
    $(this).toggleClass('selected-grocery');
})
