package com.example.data.network

data class CatalogLawItem(
    val radaId: String,
    val title: String,
    val shortTitle: String,
    val category: String,
    val description: String,
    val radaUrl: String
)

object CatalogLaws {
    val ITEMS = listOf(
        CatalogLawItem(
            radaId = "254к/96-вр",
            title = "Конституція України",
            shortTitle = "КУ",
            category = "Конституційне право",
            description = "Основний Закон України, прийнятий 28 червня 1996 року",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/254к/96-вр"
        ),
        CatalogLawItem(
            radaId = "435-15",
            title = "Цивільний кодекс України",
            shortTitle = "ЦКУ",
            category = "Цивільне право",
            description = "Основний акт цивільного законодавства України від 16.01.2003 № 435-IV",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/435-15"
        ),
        CatalogLawItem(
            radaId = "2341-14",
            title = "Кримінальний кодекс України",
            shortTitle = "ККУ",
            category = "Кримінальне право",
            description = "Кримінальний кодекс України від 05.04.2001 № 2341-III",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/2341-14"
        ),
        CatalogLawItem(
            radaId = "322-08",
            title = "Кодекс законів про працю України",
            shortTitle = "КЗпП",
            category = "Трудове право",
            description = "Регулює трудові відносини всіх працівників в Україні",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/322-08"
        ),
        CatalogLawItem(
            radaId = "2755-17",
            title = "Податковий кодекс України",
            shortTitle = "ПКУ",
            category = "Податкове та фінансове право",
            description = "Регулює сферу справляння податків і зборів в Україні",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/2755-17"
        ),
        CatalogLawItem(
            radaId = "436-15",
            title = "Господарський кодекс України",
            shortTitle = "ГКУ",
            category = "Господарське право",
            description = "Визначає основні засади господарювання в Україні",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/436-15"
        ),
        CatalogLawItem(
            radaId = "2747-15",
            title = "Кодекс адміністративного судочинства України",
            shortTitle = "КАСУ",
            category = "Адміністративне право",
            description = "Порядок здійснення судочинства в адміністративних судах",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/2747-15"
        ),
        CatalogLawItem(
            radaId = "1618-15",
            title = "Цивільний процесуальний кодекс України",
            shortTitle = "ЦПК",
            category = "Процесуальне право",
            description = "Порядок судочинства у цивільних справах",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/1618-15"
        ),
        CatalogLawItem(
            radaId = "4651-17",
            title = "Кримінальний процесуальний кодекс України",
            shortTitle = "КПК",
            category = "Процесуальне право",
            description = "Порядок кримінального провадження на території України",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/4651-17"
        ),
        CatalogLawItem(
            radaId = "80731-10",
            title = "Кодекс України про адміністративні правопорушення",
            shortTitle = "КУпАП",
            category = "Адміністративне право",
            description = "Охорона прав і свобод, власності та правопорядку",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/80731-10"
        ),
        CatalogLawItem(
            radaId = "580-19",
            title = "Закон України «Про Національну поліцію»",
            shortTitle = "ЗУ про Нацполіцію",
            category = "Адміністративне право",
            description = "Правові засади організації та діяльності Національної поліції України",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/580-19"
        ),
        CatalogLawItem(
            radaId = "1934-19",
            title = "Закон України «Про національну безпеку України»",
            shortTitle = "ЗУ про нацбезпеку",
            category = "Військове право",
            description = "Основи та принципи національної безпеки і оборони",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/1934-19"
        ),
        CatalogLawItem(
            radaId = "2232-12",
            title = "Закон України «Про військовий обов'язок і військову службу»",
            shortTitle = "ЗУ про військову службу",
            category = "Військове право",
            description = "Здійснення громадянами України священного обов'язку захисту Вітчизни",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/2232-12"
        ),
        CatalogLawItem(
            radaId = "1404-19",
            title = "Закон України «Про виконавче провадження»",
            shortTitle = "ЗУ про викон. провадження",
            category = "Процесуальне право",
            description = "Умови і порядок виконання рішень судів та інших органів",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/1404-19"
        ),
        CatalogLawItem(
            radaId = "1700-18",
            title = "Закон України «Про запобігання корупції»",
            shortTitle = "ЗУ про запобігання корупції",
            category = "Конституційне право",
            description = "Правові та організаційні засади функціонування системи запобігання корупції",
            radaUrl = "https://zakon.rada.gov.ua/laws/show/1700-18"
        )
    )
}
