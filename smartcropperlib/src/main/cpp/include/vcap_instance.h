#ifndef VCAP_INSTANCE_H_
#define VCAP_INSTANCE_H_

#include <stdint.h>
#include <iostream>
#include "define.h"

#define VCAP_VERSION_MAJOR   3
#define VCAP_VERSION_MINOR   0
#define VCAP_VERSION_PATCH   0
#define VCAP_VERSION_RELEASE 0

// Manage business-level memory
// Models with same cluster id share feature map
class VCAP_PUBLIC VCAPBusinessManager {
 public:
  VCAPBusinessManager(const VCAPBusinessManager&)  = delete;
  VCAPBusinessManager(const VCAPBusinessManager&&) = delete;
  VCAPBusinessManager& operator=(const VCAPBusinessManager&) = delete;
  VCAPBusinessManager& operator=(const VCAPBusinessManager&&) = delete;

  VCAPBusinessManager(bool using_model_path = true);

  ~VCAPBusinessManager();

  /**
   * @brief:                    register model with cluster id.
   * @param: model_cluster_id   given cluster id.
   * @param: model_path         given model path or model buffer ptr.
   * @param: model_size         given model size if model buffer passed in.
   * @birth:                    created by Kane on 20201023.
   */
  void registerModel(int model_cluster_id, const char* model_path, int model_size = 0);

  /**
   * @brief:                    get max feature map len in models with same cluster id.
   * @param:  model_cluster_id  given cluster id.
   * @return:                   max feature map len or -1 if model_cluster_id not found.
   * @birth:                    created by Kane on 20201023.
   */
  int getMaxFeatureLen(int model_cluster_id);

  /**
   * @brief:                    set model_cluster_id with given max_feature_len.
   * @param: model_cluster_id   given cluster id.
   * @param: max_feature_len    given max_feature_len.
   * @birth:                    created by Kane on 20201023.
   */
  void setMaxFeatureLen(int model_cluster_id, uint32_t max_feature_len);

  /**
   * @brief: open and parse vaimlite model, then update model_id_feature_map and
   *         model_data_size_map.
   * @birth: created by Kane on 20201023.
   */
  void finishRegistration();

 private:
  // release model model_data_size_map_
  void _releaseBuffer();
  // bool _getRegFinishFlag();
  // key   : model cluster id
  // value : model path or model buffer ptr
  std::map<int, std::vector<const char*>>* model_id_path_map_;
  // key   : model path or model buffer ptr
  // value : <model data size, model buffer ptr>
  std::map<const char*, std::pair<uint32_t, const void*>>* model_data_size_map_;
  // key   : model cluster id
  // value : <max feature map len, feature map ptr>
  std::map<int, std::pair<uint32_t, const void*>>* model_id_feature_map_;
  std::vector<void *> file_reader_;
  bool reg_finished_     = false;
  bool using_model_path_ = true;
};

class VCAP_PUBLIC VcapInstance {
 public:
  VcapInstance(const VcapInstance&)  = delete;
  VcapInstance(const VcapInstance&&) = delete;
  VcapInstance& operator=(const VcapInstance&) = delete;
  VcapInstance& operator=(const VcapInstance&&) = delete;

  VcapInstance() = default;

  ~VcapInstance();

  /**
   * @brief:         create the network using config.
   * @param: config  properties required to create a network.
   *                 for example if you set the following properties:
   *                -------------------------------------------
   *                | config.device_type   = VCAP_ARM_CPU;    |
                    | config.output_format = "NCHW";          |
                    | config.model_file    = model_file_path; |
                    -------------------------------------------
                     it means the network will run on arm cpu and the output
                     format is "NCHW"(RRRRGGGGBBBB).
   * @return:        true if the network has been successfully created.
   * @birth:         created by Zhangsuchi on 20201030.
   */
  bool createNeuralNetwork(vcap::BuildConfig& config) {
    return _createNeuralNetworkInner(config, VCAP_VERSION_MAJOR, VCAP_VERSION_MINOR, VCAP_VERSION_PATCH, VCAP_VERSION_RELEASE);
  }

  /**
   * @brief:              set the input data for network.
   * @param: name         input feature name
   * @param: data         pointer of input data.
   * @param: batch        the batch of input data.
   * @param: channel      the channel of input data.
   * @param: height       the height of input data.
   * @param: width        the width of input data.
   * @param: byte_size    size in bytes of input data.
   * @return:             status of success or failure.
   * @birth:              created by Zhangsuchi on 20201030.
   */
  bool setInput(const char* name, const void* data, int batch, int channel, int height, int width, int byte_size);

  /**
   * @brief:    execute the neural network.
   * @return:   status of success or failure.
   * @birth:    created by Zhangsuchi on 20201030.
   */
  bool forward();

  /**
   * @brief:              get the data of a given output feature, the given output feature name
   *                      must be in the output name list.
   * @param: name         output feature name.
   * @param: data         pointer of output data, it should be noted that the memory of output
   *                      data needs to be allocated by users. If the allocated size is smaller
   *                      than the output size, it will cause Out-of-Bounds Access.
   * @return:             status of success or failure.
   * @birth:              created by Zhangsuchi on 20201030.
   */
  bool getOutput(const char* name, void* data);

  /**
   * @brief:              get the shape of a given output feature map, the given output feature
   *                      name must be in the output name list.
   * @param: name         output feature name
   * @param: batch        the batch of output data.
   * @param: channel      the channel of output data.
   * @param: height       the height of output data.
   * @param: width        the width of output data.
   * @return:             status of success or failure.
   * @birth:              created by Zhangsuchi on 20201030.
   */
  bool getOutputShape(const char* name, int& batch, int& channel, int& height, int& width);

  /**
   * @brief:    get the version of vcap.
   * @return:   the version of vcap.
   * @birth:    created by Zhangsuchi on 20201030.
   */
  const char* getVcapVersion();

 private:
  bool _createNeuralNetworkInner(vcap::BuildConfig& config, int major, int minor, int patch, int release);

  void* netspace_ = nullptr;
  //a temp param for dsp, will delete in the future
  long dsp_handle_;
};

#endif
