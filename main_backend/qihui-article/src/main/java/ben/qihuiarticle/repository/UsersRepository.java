package ben.qihuiarticle.repository;

import ben.qihuiarticle.entity.entity_user.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Integer> {
    Users findByUserId(long userId);
}
