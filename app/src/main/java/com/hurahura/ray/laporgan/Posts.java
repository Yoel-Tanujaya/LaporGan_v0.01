package com.hurahura.ray.laporgan;

public class Posts {
    private String name;
    private String jenisLaporan;
    private String time;
    private String description;
    private String imgLaporanUri;
    private String imgUserUri;

    Posts() {}

    Posts(String name, String jenisLaporan, String time, String description, String imgLaporanUri, String imgUserUri) {
        this.setName(name);
        this.setJenisLaporan(jenisLaporan);
        this.setTime(time);
        this.setDescription(description);
        this.setImgLaporanUri(imgLaporanUri);
        this.setImgUserUri(imgUserUri);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJenisLaporan() {
        return jenisLaporan;
    }

    public void setJenisLaporan(String jenisLaporan) {
        this.jenisLaporan = jenisLaporan;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgLaporanUri() {
        return imgLaporanUri;
    }

    public void setImgLaporanUri(String imgLaporanUri) {
        this.imgLaporanUri = imgLaporanUri;
    }

    public String getImgUserUri() {
        return imgUserUri;
    }

    public void setImgUserUri(String imgUserUri) {
        this.imgUserUri = imgUserUri;
    }
}
