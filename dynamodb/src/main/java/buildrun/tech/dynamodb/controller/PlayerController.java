package buildrun.tech.dynamodb.controller;

import buildrun.tech.dynamodb.entity.PlayerHistoryEntity;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;

@RestController
@RequestMapping("/v1/players")
public class PlayerController {

    private DynamoDbTemplate dynamodDbTemplate;

    public PlayerController(DynamoDbTemplate dynamodDbTemplate) {
        this.dynamodDbTemplate = dynamodDbTemplate;
    }

    @PostMapping("/{username}/games")
    public ResponseEntity<Void> save(@PathVariable("username") String username,
                                     @RequestBody ScoreDto scoreDto) {

        var entity = PlayerHistoryEntity.fromScore(username, scoreDto);

        dynamodDbTemplate.save(entity);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/games")
    public ResponseEntity<List<PlayerHistoryEntity>> getAll(@PathVariable("username") String username) {

        var key = Key.builder().partitionValue(username).build();

        var condition = QueryConditional.keyEqualTo(key);

        var query = QueryEnhancedRequest.builder()
                .queryConditional(condition)
                .build();

        var history = dynamodDbTemplate.query(query, PlayerHistoryEntity.class);

        return ResponseEntity.ok(history.items().stream().toList());
    }

    @GetMapping("/{username}/games/{gameId}")
    public ResponseEntity<PlayerHistoryEntity> findById(@PathVariable("username") String username,
                                                        @PathVariable("gameId") String gameId) {
        var entity = dynamodDbTemplate.load(Key.builder()
                .partitionValue(username)
                .sortValue(gameId)
                .build(), PlayerHistoryEntity.class);

        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }

    @PutMapping("/{username}/games/{gameId}")
    public ResponseEntity<Void> update(@PathVariable("username") String username,
                                       @PathVariable("gameId") String gameId,
                                       @RequestBody ScoreDto scoreDto) {
        var entity = dynamodDbTemplate.load(Key.builder()
                .partitionValue(username)
                .sortValue(gameId)
                .build(), PlayerHistoryEntity.class);

        if (entity == null) {
            return ResponseEntity.notFound().build();
        }

        entity.setScore(scoreDto.score());

        dynamodDbTemplate.update(entity);

        return ResponseEntity.noContent().build();


    }

    @DeleteMapping("/{username}/games/{gameId}")
    public ResponseEntity<Void> delete(@PathVariable("username") String username,
                                       @PathVariable("gameId") String gameId,
                                       @RequestBody ScoreDto scoreDto) {
        var entity = dynamodDbTemplate.load(Key.builder()
                .partitionValue(username)
                .sortValue(gameId)
                .build(), PlayerHistoryEntity.class);

        if (entity == null) {
            return ResponseEntity.notFound().build();
        }

        dynamodDbTemplate.delete(entity);

        return ResponseEntity.noContent().build();


    }

}
