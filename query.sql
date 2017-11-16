/*    ==Параметры сценариев==

    Версия исходного сервера : SQL Server 2017 (14.0.1000)
    Выпуск исходного ядра СУБД : Выпуск Microsoft SQL Server Express Edition
    Тип исходного ядра СУБД : Изолированный SQL Server

    Версия целевого сервера : SQL Server 2017
    Выпуск целевого ядра СУБД : Выпуск Microsoft SQL Server Express Edition
    Тип целевого ядра СУБД : Изолированный SQL Server
*/

/****** Object:  Table [dbo].[IMAGES]    Script Date: 12.11.2017 14:20:48 ******/
DROP TABLE [dbo].[IMAGES]
GO

/****** Object:  Table [dbo].[IMAGES]    Script Date: 12.11.2017 14:20:48 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[IMAGES](
[ID] [int] IDENTITY(1,1) NOT NULL,
[IDGIFTS] [int] NULL,
[PATH] [varchar](max) NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

/****** Object:  Table [dbo].[GIFTS]    Script Date: 12.11.2017 14:22:49 ******/
DROP TABLE [dbo].[GIFTS]
GO

/****** Object:  Table [dbo].[GIFTS]    Script Date: 12.11.2017 14:22:49 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[GIFTS](
[ID] [int] IDENTITY(1,1) NOT NULL,
[NAME] [varchar](max) NULL,
[DESCRIBE] [varchar](max) NULL,
[CATEGORY] [varchar](max) NULL,
[URL] [varchar](max) NULL,
[COST] [float] NULL,
[CODE] [varchar](50) NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO