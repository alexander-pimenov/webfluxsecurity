package net.proselyte.webfluxsecurity.mapper;

import net.proselyte.webfluxsecurity.dto.UserDto;
import net.proselyte.webfluxsecurity.entity.UserEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

/*Это Mapstruct. Должен быть прямой и обратный метод.*/
//componentModel = "spring" - говорит, что мы что-то делаем на основании контекста.
@Mapper(componentModel = "spring")
public interface UserMapper {
    //это прямой метод
    UserDto map(UserEntity userEntity);

    //это обратный метод
    @InheritInverseConfiguration
    UserEntity map(UserDto dto);
}
