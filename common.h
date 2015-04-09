#define MAX_BUFFER_LENGTH (128 - sizeof(uint))
#define MAX_PASSWORD_LENGTH (64-sizeof(uint))
typedef struct {
  uint size_bytes;
  char password[MAX_PASSWORD_LENGTH];
} password_t;

typedef struct {
  uint v[4];
} password_hash_t;

typedef struct {
  uint size;
  char buffer[MAX_BUFFER_LENGTH];
} buffer_t;

void md5(const char* restrict msg, uint length_bytes, uint* restrict out);
void md5_buffer(const buffer_t* in, buffer_t* out);
