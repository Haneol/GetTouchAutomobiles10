package com.example.gta.data.model

object CarDataManager {
    val carList = mutableListOf<Car>().apply {
        add(Car(id = "1", name = "쏘나타 디 엣지", image = "https://www.hyundai.com/contents/repn-car/side-45/main-sonata-the-edge-25my-45side.png"))
        add(Car(id = "2", name = "쏘나타 디 엣지 Hybrid", image = "https://www.hyundai.com/contents/repn-car/side-45/main-sonata-the-edge-hybrid-25my-45side.png"))
        add(Car(id = "3", name = "아반떼", image = "https://www.hyundai.com/contents/repn-car/side-45/main-avante-25my-45side.png"))
        add(Car(id = "4", name = "아반떼 Hybrid", image = "https://www.hyundai.com/contents/repn-car/side-45/main-avante-hybrid-25my-45side.png"))
        add(Car(id = "5", name = "그랜저", image = "https://www.hyundai.com/contents/repn-car/side-45/main-grandeur-25my-45side.png"))
        add(Car(id = "6", name = "그랜저 Hybrid", image = "https://www.hyundai.com/contents/repn-car/side-45/main-grandeur-hybrid-25my-45side.png"))
    }
}