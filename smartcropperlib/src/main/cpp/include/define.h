#ifndef VCAP_DEFINE_H_
#define VCAP_DEFINE_H_

#include <string>
#include <vector>
#include <map>
#include <utility>
#include "types.h"

#define VCAP_PUBLIC  __attribute__((visibility("default")))
#define VCAP_PRIVATE __attribute__((visibility("hidden")))

namespace vcap {

struct RuntimeConfig {
  MemStrategy memory = COMMON;

  PowerMode power = HIGH_PERFORMANCE;

  PrecisionMode precision = PRECISION_FP16;

  SrcFramework framework;

  // vaimlite::DataFormat feature_format;

  DataFormat feature_format;

  // vaimlite::DataFormat output_format;

  DataFormat output_format;

  DType dtype;

  bool keep_model_memory = true;

  // vaimlite::DataType dtype;

  vcap::QuantType quantize_type;

  /**
   OpenCLRuntime config
  **/
  GPUPriority priority = NORMAL_PRIORITY;

  const char* gpu_cache_path = "/sdcard/binary.bin";

  int cpu_num_threads = 4;

  /**
   HTARuntime config
  **/
  const char* hta_cache_path = "";
  const char* hta_library_path = "";

  /**
   DSPRuntime config
  **/
 const char* hvx_library_path = "";
};

struct BuildConfig {
  /** device type */
  VCAPDeviceType device_type = VCAPDeviceType::VCAP_ARM_CPU;

  const char* model_file = nullptr;

  const char* output_format = "NCHW";

  int log_level = LogLevel::INFO;

  int model_length = -1;

  bool encrypt = false;

  RuntimeConfig runtime_config;
};
}  // namespace vcap

#endif