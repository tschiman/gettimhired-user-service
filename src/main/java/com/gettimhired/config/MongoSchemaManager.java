package com.gettimhired.config;

import com.gettimhired.model.mongo.ChangeSet;
import com.gettimhired.model.mongo.User;
import com.gettimhired.repository.ChangeSetRepository;
import com.gettimhired.service.UserService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
public class MongoSchemaManager {
    Logger log = LoggerFactory.getLogger(MongoSchemaManager.class);
    private final MongoTemplate mongoTemplate;

    private final UserService userService;
    private final ChangeSetRepository changeSetRepository;

    public MongoSchemaManager(MongoTemplate mongoTemplate, UserService userService, ChangeSetRepository changeSetRepository) {
        this.mongoTemplate = mongoTemplate;
        this.userService = userService;
        this.changeSetRepository = changeSetRepository;
    }

    @PostConstruct
    public void init() {
        doChangeSet(
                "changeset-001",
                "tim.schimandle",
                "index to email on User",
                () -> {
                    var index = new Index()
                            .on("email", Sort.Direction.ASC).background();
                    mongoTemplate.indexOps(User.class).ensureIndex(index);
                }
        );
        doChangeSet(
                "changeset-002",
                "tim.schimandle",
                "migrate users from main application",
                () -> {
                    userService.migrateUsers();
                }
        );
    }

    private void doChangeSet(String id, String author, String description, Runnable change) {
        log.debug("Starting change set |id: {} |author: {} |description: {}", id, author, description);
        var changeSet = new ChangeSet();
        changeSet.setId(id);
        changeSet.setAuthor(author);
        changeSet.setDescription(description);
        changeSet.setCreatedDate(System.currentTimeMillis());

        var changeSetFromDb = changeSetRepository.findById(id);

        if (
                changeSetFromDb.isEmpty() || //change set doesn't exist
                (!changeSetFromDb.get().isInProgress() && !changeSetFromDb.get().isCompleted()) //change is not complete and not in progress
        ) {
            try {
                changeSetRepository.save(changeSet); //starts work
                //do the work
                log.info("Running change set |id: {} |author: {} |description: {}", id, author, description);
                change.run();

                //update the changeset
                var changeSet1 = changeSetRepository.findById(changeSet.getId());
                changeSet1.ifPresent(c -> {
                    c.setInProgress(false);
                    c.setCompleted(true);
                    changeSetRepository.save(c);
                });
            } catch (Exception e) {
                log.debug("Error with change set |id: {} |author: {} |description: {}", id, author, description);
                //fail the changeset if there's an issue
                changeSet.setInProgress(false);
                changeSet.setCompleted(false);
                changeSet.setDescription(e.getMessage());
                changeSetRepository.save(changeSet);
                //throw the exception again to stop initialization
                throw e;
            }
        } else {
            //skip, already applied
        }
        log.debug("Completed change set |id: {} |author: {} |description: {}", id, author, description);
    }
}
