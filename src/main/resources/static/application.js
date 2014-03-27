var name, bytes, currentGallery;
var repositories = sdr.createRepositories();
var itemRepository = repositories.itemRepository;
var galleryRepository = repositories.galleryRepository;

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

function pickGallery(id) {
    currentGallery = id;
}

function deletePic(id) {
    itemRepository.delete(id).done(function() {
        window.location.reload();
    });
}

function addTo(id) {
    var item = itemRepository.findOne(id);
    var gallery = galleryRepository.findOne(currentGallery);

    $.when(item, gallery).then(function(itemData, galleryData){
        var addItems = galleryRepository.addItems(galleryData[0], itemData[0]);
        var setGallery = itemRepository.setGallery(itemData[0], galleryData[0]);

        $.when(addItems, setGallery).then(function(){
            window.location.reload();
        });
    });
}

function removePic(itemId, galleryId) {
    console.log("Working on ability to remove item " + itemId + " from gallery " + galleryId);
}
