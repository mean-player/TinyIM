package com.example.demo.Mapper;



import com.example.demo.Entity.Application;
import com.example.demo.Provider.ApplicationProvider;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ApplicationMapper {

    @InsertProvider(type = ApplicationProvider.class, method = "addApplication")
    void addApplication(Application application);

    @SelectProvider(type = ApplicationProvider.class, method = "selectById")
    Application selectById(@Param("id") Long id);

    @SelectProvider(type = ApplicationProvider.class, method = "selectByFT")
    Application selectByFT(@Param("from_id") Long fromId,
                           @Param("to_id") Long toId);

    @UpdateProvider(type = ApplicationProvider.class, method = "updateStatus")
    void updateStatus(@Param("id") Long id,
                      @Param("status") Integer status);

    @UpdateProvider(type = ApplicationProvider.class, method = "updateRead")
    void updateRead(@Param("id") Long id,
                    @Param("is_read") Boolean isRead);

    @SelectProvider(type = ApplicationProvider.class, method = "selectSentApplication")
    List<Application> selectSentApplication(@Param("from_id") Long fromId);

    @SelectProvider(type = ApplicationProvider.class, method = "selectReceiveApplication")
    List<Application> selectReceiveApplication(@Param("to_id") Long toId);
}
