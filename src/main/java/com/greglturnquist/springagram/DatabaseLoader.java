package com.greglturnquist.springagram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Profile("!production")
public class DatabaseLoader {

    private final GalleryRepository galleryRepository;
    private final ItemRepository itemRepository;
    private final ApplicationContext ctx;

    @Autowired
    public DatabaseLoader(GalleryRepository galleryRepository, ItemRepository itemRepository, ApplicationContext ctx) {
        this.galleryRepository = galleryRepository;
        this.itemRepository = itemRepository;
        this.ctx = ctx;
    }

    @PostConstruct
    public void init() throws IOException {
        // No demo is complete without pre-loading some cats

        Item cat = itemRepository.save(createItem(ctx.getResource("classpath:cat.jpg")));
        Item caterpillar = itemRepository.save(createItem(ctx.getResource("classpath:caterpillar.jpg")));

        GalleryRepository galleryRepository = ctx.getBean(GalleryRepository.class);

        Gallery catGallery = new Gallery();
        catGallery.setDescription("Collection of cats");
        catGallery = galleryRepository.save(catGallery);

        Gallery truckGallery = new Gallery();
        truckGallery.setDescription("Collection of trucks");
        truckGallery = galleryRepository.save(truckGallery);

        cat.setGallery(catGallery);
        itemRepository.save(cat);

//		caterpillar.setGallery(truckGallery);
//		itemRepository.save(caterpillar);
    }

    private static Item createItem(Resource file) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileCopyUtils.copy(file.getInputStream(), output);
        Item item = new Item();
        item.setName(file.getFilename());
        item.setImage("data:image/png;base64," + DatatypeConverter.printBase64Binary(output.toByteArray()));
        return item;
    }

}
