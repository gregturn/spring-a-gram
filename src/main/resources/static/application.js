var name, bytes;
var itemRepository = sdr.createRepositories().itemRepository;

function readImage(input) {
    if (input.files && input.files[0]) {
        if (input.files[0].type.indexOf('image') != -1) {
            var FR = new FileReader();
            FR.onloadend = function () {
                name = input.files[0].name;
                bytes = FR.result;
            }
            FR.readAsDataURL(input.files[0]);
        }
    }
}

$('#file').change(function() {
    readImage(this);
});

$('#upload').submit(function(e) {
    e.preventDefault();
    itemRepository.create({
        name: name,
        image: bytes
    }).done(function() {
        window.location.reload();
    })
});

function deletePic(id) {
    itemRepository.delete(id).done(function() {
        window.location.reload();
    });
}
