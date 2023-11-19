package cn.bitoffer.shorturlx.controller;

import cn.bitoffer.shorturlx.common.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("/shorturlx")
@Slf4j
public class ShortUrlController {

    @Autowired
    private ShortUrlService shortUrlService;

    // v1端点
    @GetMapping("/v1/{short_url}")
    public ResponseEntity<String> redirectToLongUrlV1(@PathVariable String short_url) {
        String longUrl = shortUrlService.getV1LongUrl(short_url);
        return ResponseEntity.ok(longUrl);
    }

    @PostMapping("/v1/shorten")
    public ResponseEntity<String> createShortUrlV1(@RequestBody String longUrl) {
        String shortUrl = shortUrlService.createV1ShortUrl(longUrl);
        return ResponseEntity.ok(shortUrl);
    }

    // v2端点
    @GetMapping("/v2/{short_url}")
    public ResponseEntity<String> redirectToLongUrlV2(@PathVariable String short_url) {
        String longUrl = shortUrlService.getV2LongUrl(short_url);
        return ResponseEntity.ok(longUrl);
    }

    @PostMapping("/v2/shorten")
    public ResponseEntity<String> createShortUrlV2(@RequestBody String longUrl) {
        String shortUrl = shortUrlService.createV2ShortUrl(longUrl);
        return ResponseEntity.ok(shortUrl);
    }

    // v3端点
    @GetMapping("/v3/{short_url}")
    public ResponseEntity<String> redirectToLongUrlV3(@PathVariable String short_url) {
        String longUrl = shortUrlService.getV3LongUrl(short_url);
        return ResponseEntity.ok(longUrl);
    }

    @PostMapping("/v3/shorten")
    public ResponseEntity<String> createShortUrlV3(@RequestBody String longUrl) {
        String shortUrl = shortUrlService.createV3ShortUrl(longUrl);
        return ResponseEntity.ok(shortUrl);
    }
}
