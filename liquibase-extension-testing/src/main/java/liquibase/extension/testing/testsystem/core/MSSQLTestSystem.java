package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;

public class MSSQLTestSystem extends DatabaseTestSystem {

    public MSSQLTestSystem() {
        super("mssql");
    }

    @Override
    protected @NotNull DatabaseWrapper createWrapper() {
        return new DockerDatabaseWrapper(new MSSQLServerContainer(
                DockerImageName.parse(getImageName()).withTag(getVersion())),
                this
        );
    }

    @Override
    public String getUrl() {
        final JdbcDatabaseContainer container = ((DockerDatabaseWrapper) wrapper).getContainer();

        return "jdbc:sqlserver://" + container.getHost() + ":" + container.getMappedPort(MSSQLServerContainer.MS_SQL_SERVER_PORT)+";databaseName="+getCatalog();
    }
    @Override
    protected String[] getSetupSql() {
        return new String[]{
                "CREATE LOGIN [" + getUsername() + "] with password=N'" + getPassword() + "', CHECK_EXPIRATION=OFF",

                "CREATE DATABASE " + getCatalog(),
                "EXEC lbcat..sp_addsrvrolemember @loginame = N'" + getUsername() + "', @rolename = N'sysadmin'",

                "USE [" + getCatalog() + "]",
//                "ALTER DATABASE ["+getCatalog()+"] MODIFY FILEGROUP [PRIMARY] DEFAULT",
                "ALTER DATABASE [" + getCatalog() + "] ADD FILEGROUP [" + getAltTablespace() + "]",

                "ALTER DATABASE [" + getCatalog() + "] ADD FILE ( NAME = N'" + getAltTablespace() + "', FILENAME = N'/tmp/" + getAltTablespace() + ".ndf' , SIZE = 8192KB , FILEGROWTH = 65536KB ) TO FILEGROUP [" + getAltTablespace() + "]",
                "CREATE SCHEMA [" + getAltSchema() + "] AUTHORIZATION [dbo]",
        };
    }
}
