public class EnterpriseFiler {

    public static void file(String base64) throws Exception {

        /*byte[] bytes = Base64.getDecoder().decode(base64);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ZipInputStream zis = new ZipInputStream(bais);

        try {
            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                byte[] buffer = new byte[2048];

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(baos, buffer.length);

                int len = 0;
                while ((len = zis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }

                bos.flush();
                bos.close();

                String csvFileName = entry.getName();
                byte[] csvFileBytes = baos.toByteArray();
                writeFile(csvFileName, directory, csvFileBytes);
            }
        } finally {
            zis.close();
        }*/
    }
}
