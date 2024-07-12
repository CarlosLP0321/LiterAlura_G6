package com.aluracursos.literaluraCl.service;

public interface IConvierteDatos {
    <T> T obtenerDatos(String json, Class<T> clase);
}
